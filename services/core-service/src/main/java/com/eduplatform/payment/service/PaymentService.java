package com.eduplatform.payment.service;

import com.eduplatform.payment.model.*;
import com.eduplatform.payment.repository.*;
import com.eduplatform.payment.dto.*;
import com.eduplatform.payment.exception.PaymentException;
import com.eduplatform.core.course.model.Course;
import com.eduplatform.core.course.repository.CourseRepository;
import com.eduplatform.core.user.model.User;
import com.eduplatform.core.user.repository.UserRepository;
import com.eduplatform.core.enrollment.model.Enrollment;
import com.eduplatform.core.enrollment.repository.EnrollmentRepository;
import com.eduplatform.notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private EmailService emailService;

    // Create Order for Course Purchase
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userId) {
        try {
            // Validate course
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new PaymentException("Course not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new PaymentException("User not found"));

            // Check if already enrolled
            Optional<Enrollment> existingEnrollment = enrollmentRepository
                    .findByUserIdAndCourseIdAndTenantId(userId, request.getCourseId(), user.getTenantId());
            if (existingEnrollment.isPresent()) {
                throw new PaymentException("User already enrolled in this course");
            }

            // Calculate amount
            BigDecimal amount = course.getPrice();
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal tax = amount.multiply(new BigDecimal("0.18")); // 18% GST
            BigDecimal totalAmount = amount.add(tax).subtract(discount);

            // Create Razorpay order
            Map<String, String> notes = new HashMap<>();
            notes.put("userId", userId);
            notes.put("courseId", request.getCourseId());
            notes.put("courseName", course.getTitle());

            JSONObject razorpayOrder = razorpayService.createOrder(
                    totalAmount,
                    "INR",
                    "ORDER-" + System.currentTimeMillis(),
                    notes
            );

            // Save order in database
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUserId(userId);
            order.setCourseId(request.getCourseId());
            order.setRazorpayOrderId(razorpayOrder.getString("id"));
            order.setSubtotal(amount);
            order.setTax(tax);
            order.setDiscount(discount);
            order.setTotalAmount(totalAmount);
            order.setCurrency("INR");
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            order.setPaymentAttempts(0);
            order.setTenantId(user.getTenantId());

            orderRepository.save(order);

            // Build response
            OrderResponse response = new OrderResponse();
            response.setOrderId(order.getId());
            response.setRazorpayOrderId(razorpayOrder.getString("id"));
            response.setTotalAmount(totalAmount);
            response.setStatus("PENDING");
            response.setExpiresAt(order.getExpiresAt());

            return response;

        } catch (Exception e) {
            log.error("Error creating order", e);
            throw new PaymentException("Failed to create order: " + e.getMessage());
        }
    }

    // Verify and Process Payment
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request, String userId) {
        try {
            // Verify signature
            if (!razorpayService.verifyPaymentSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature())) {
                throw new PaymentException("Invalid payment signature");
            }

            // Get order
            Order order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new PaymentException("Order not found"));

            // Get payment details from Razorpay
            JSONObject paymentDetails = razorpayService.getPaymentDetails(request.getRazorpayPaymentId());

            // Create Payment record
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setUserId(userId);
            payment.setCourseId(order.getCourseId());
            payment.setRazorpayOrderId(request.getRazorpayOrderId());
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency("INR");
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setPaymentGateway("RAZORPAY");

            if (paymentDetails.has("method")) {
                payment.setPaymentMethod(resolvePaymentMethod(paymentDetails.getString("method")));
            }

            if (paymentDetails.has("card")) {
                JSONObject card = paymentDetails.getJSONObject("card");
                if (card.has("last4")) {
                    payment.setCardLast4(card.getString("last4"));
                }
                if (card.has("issuer")) {
                    payment.setCardIssuer(card.getString("issuer"));
                }
            }

            if (paymentDetails.has("vpa")) {
                payment.setUpiVpa(paymentDetails.getString("vpa"));
            }

            payment.setDescription("Payment for course: " + order.getCourseId());
            payment.setReceiptId("RECEIPT-" + System.currentTimeMillis());
            payment.setCapturedAt(LocalDateTime.now());
            payment.setCreatedAt(LocalDateTime.now());
            payment.setTenantId(order.getTenantId());

            paymentRepository.save(payment);

            // Update order status
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            orderRepository.save(order);

            // Create enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setId(UUID.randomUUID().toString());
            enrollment.setUserId(userId);
            enrollment.setCourseId(order.getCourseId());
            enrollment.setStatus("ACTIVE");
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setProgressPercentage(0.0);
            enrollment.setCompletedLessons(new ArrayList<>());
            enrollment.setTenantId(order.getTenantId());

            enrollmentRepository.save(enrollment);

            // Generate invoice
            invoiceService.generateInvoice(payment, order);

            // Send confirmation email
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                emailService.sendPaymentSuccessEmail(user, order, payment);
            }

            // Build response
            PaymentResponse response = new PaymentResponse();
            response.setPaymentId(payment.getId());
            response.setOrderId(order.getId());
            response.setAmount(payment.getAmount());
            response.setStatus("CAPTURED");
            response.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "UNKNOWN");
            response.setCourseId(order.getCourseId());
            response.setCreatedAt(payment.getCreatedAt());
            response.setCapturedAt(payment.getCapturedAt());

            return response;

        } catch (Exception e) {
            log.error("Error verifying payment", e);
            throw new PaymentException("Payment verification failed: " + e.getMessage());
        }
    }

    // Get Payment History
    public List<PaymentResponse> getPaymentHistory(String userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Refund Payment
    @Transactional
    public void refundPayment(String paymentId, BigDecimal refundAmount, String reason) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new PaymentException("Payment not found"));

            // Create refund in Razorpay
            JSONObject refund = razorpayService.createRefund(
                    payment.getRazorpayPaymentId(),
                    refundAmount,
                    reason
            );

            // Update payment record
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundAmount(refundAmount);
            payment.setRefundReason(reason);
            payment.setRefundedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            // Send refund email
            User user = userRepository.findById(payment.getUserId()).orElse(null);
            if (user != null) {
                emailService.sendRefundEmail(user, payment, refundAmount);
            }

            log.info("Refund processed for payment: {}", paymentId);

        } catch (Exception e) {
            log.error("Error processing refund", e);
            throw new PaymentException("Refund failed: " + e.getMessage());
        }
    }

    // Get Payment Details
    public PaymentResponse getPaymentDetails(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));

        return convertToResponse(payment);
    }

    private PaymentResponse convertToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setOrderId(orderRepository.findByRazorpayOrderId(payment.getRazorpayOrderId())
                .map(Order::getId)
                .orElse(payment.getRazorpayOrderId()));
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus().toString());
        response.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "UNKNOWN");
        response.setCourseId(payment.getCourseId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setCapturedAt(payment.getCapturedAt());
        return response;
    }

    private PaymentMethod resolvePaymentMethod(String method) {
        if (method == null || method.isBlank()) {
            return null;
        }

        return switch (method.trim().toLowerCase()) {
            case "upi" -> PaymentMethod.UPI;
            case "netbanking" -> PaymentMethod.NETBANKING;
            case "wallet" -> PaymentMethod.WALLET;
            case "emi" -> PaymentMethod.EMI;
            default -> PaymentMethod.CARD;
        };
    }
}

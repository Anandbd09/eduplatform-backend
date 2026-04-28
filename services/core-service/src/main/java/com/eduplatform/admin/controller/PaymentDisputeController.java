package com.eduplatform.admin.controller;

import com.eduplatform.admin.dto.DisputeResolutionRequest;
import com.eduplatform.admin.service.PaymentManagementService;
import com.eduplatform.core.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/disputes")
public class PaymentDisputeController {

    @Autowired
    private PaymentManagementService paymentManagementService;

    @PostMapping("/{disputeId}/resolve")
    public ResponseEntity<?> resolveDispute(@RequestHeader("X-User-Id") String adminId,
                                            @PathVariable String disputeId,
                                            @RequestBody DisputeResolutionRequest request) {
        try {
            paymentManagementService.resolveDispute(
                    adminId,
                    disputeId,
                    request.getResolution(),
                    request.getRefundAmount(),
                    request.getNotes()
            );
            return ResponseEntity.ok(ApiResponse.success(null, "Dispute resolved"));
        } catch (Exception e) {
            log.error("Error resolving dispute {}", disputeId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "DISPUTE_RESOLVE_FAILED"));
        }
    }

    @GetMapping("/open")
    public ResponseEntity<?> getOpenDisputes() {
        try {
            var disputes = paymentManagementService.getOpenDisputes();
            return ResponseEntity.ok(ApiResponse.success(disputes, "Open disputes"));
        } catch (Exception e) {
            log.error("Error fetching open disputes", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "DISPUTE_FETCH_FAILED"));
        }
    }
}

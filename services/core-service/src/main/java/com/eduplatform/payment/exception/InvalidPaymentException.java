package com.eduplatform.payment.exception;

public class InvalidPaymentException extends PaymentException {

    public InvalidPaymentException() {
        super();
    }

    public InvalidPaymentException(String message) {
        super(message);
    }

    public InvalidPaymentException(Throwable cause) {
        super(cause);
    }

    public InvalidPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}

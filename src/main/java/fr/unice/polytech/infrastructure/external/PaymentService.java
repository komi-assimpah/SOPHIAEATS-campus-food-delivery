package fr.unice.polytech.infrastructure.external;

import fr.unice.polytech.application.port.IPaymentService;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.payment.PaymentResult;

public class PaymentService implements IPaymentService {
    // This is a dummy implementation of the payment service
    // It will be replaced by an external payment service

    @Override
    public PaymentResult processPayment(Order order, PaymentDetails paymentDetails) {
        // boolean success = Math.random() < 0.9; // 90% success rate
        boolean success = true;
        return new PaymentResult(success, success ? "Payment successful" : "Payment failed");
    }
}

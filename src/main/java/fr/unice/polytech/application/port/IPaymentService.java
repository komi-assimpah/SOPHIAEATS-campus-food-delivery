package fr.unice.polytech.application.port;

import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.payment.PaymentResult;

public interface IPaymentService {
    PaymentResult processPayment(Order order, PaymentDetails paymentDetails);
}

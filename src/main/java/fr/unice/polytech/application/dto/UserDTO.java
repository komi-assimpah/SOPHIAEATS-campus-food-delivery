package fr.unice.polytech.application.dto;

import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.domain.models.user.UserStatus;

import java.util.ArrayList;
import java.util.List;

public record UserDTO(
        String id,
        String name,
        String email,
        String password,
        double balance,
        UserStatus type,
        List<PaymentDetails> paymentDetails
) {
    public UserDTO {
        // Initialize payment details if null is passed to avoid null pointer exceptions
        paymentDetails = (paymentDetails == null) ? new ArrayList<>() : paymentDetails;
    }
}

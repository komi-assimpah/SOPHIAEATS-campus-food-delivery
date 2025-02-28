package fr.unice.polytech.domain.models.payment;

public class  PaymentResult {
    boolean success;
    String message;

    public PaymentResult(boolean success, String message){
        this.success = success ;
        this.message = message;
    }

    public boolean success() {
        return success ;
    }

    public String message() {
        return message;
    }
}

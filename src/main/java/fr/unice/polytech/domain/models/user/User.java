package fr.unice.polytech.domain.models.user;

import fr.unice.polytech.domain.models.payment.PaymentDetails;
import fr.unice.polytech.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private final String id;
    private String name;
    private String email;
    private String password;
    private double balance;
    private UserStatus type;
    private List<PaymentDetails> paymentDetails ;

    public User(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.balance = 0;
        this.password = password;
        this.type = UserStatus.CAMPUS_STUDENT;
        this.paymentDetails = new ArrayList<>();
    }

    public User(String id, String name, String email, String password , List<PaymentDetails> paimentDetails) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.balance = 0;
        this.password = password;
        this.type = UserStatus.CAMPUS_STUDENT;
        this.paymentDetails = paimentDetails;
    }

    public User(String name, String email, String password) {
        this(Utils.generateUniqueId(), name, email, password);
    }

    public User(){
        this.id = null ;
    }

    //getters
    public String getId() {
        return this.id;
    }

    public UserStatus getType(){
        return type;
    }

    public String getEmail() {
        return email;
    }

    public String getName(){
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public String getPassword(){
        return password;
    }

    public List<PaymentDetails> getPaymentDetails() {
        return paymentDetails;
    }

    //setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(UserStatus type) {
        this.type = type;
    }

    public boolean addPaymentMethod(PaymentDetails paymentDetails){
        return this.paymentDetails.add(paymentDetails);
    }

    public void addToBalance(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add a negative amount");
        }
        this.balance += amount;
    }

    public void withdrawFromBalance(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot withdraw a negative amount");
        }
        if (this.balance < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance -= amount;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' + // Consider removing or masking this for security reasons
                ", balance=" + balance +
                ", type=" + type +
                ", paymentDetails=" + paymentDetails +
                '}';
    }




}

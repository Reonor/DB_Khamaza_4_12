package ua.zlagoda.model.dto;

/** One row of the «клієнти, що купували товари всіх категорій» query result. */
public class LoyalCustomerRow {

    private String cardNumber;
    private String fullName;
    private String phoneNumber;

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}

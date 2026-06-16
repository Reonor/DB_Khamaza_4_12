package ua.zlagoda.model;

import jakarta.validation.constraints.*;

/** «Карта клієнта». */
public class CustomerCard {

    @NotBlank
    @Size(max = 13)
    private String cardNumber;

    @NotBlank @Size(max = 50)
    private String custSurname;

    @NotBlank @Size(max = 50)
    private String custName;

    @Size(max = 50)
    private String custPatronymic;

    @NotBlank
    @Size(max = 13, message = "Номер телефону не може перевищувати 13 символів")
    private String phoneNumber;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String street;

    @Size(max = 9)
    private String zipCode;

    @NotNull
    @Min(value = 0, message = "Відсоток не може бути від'ємним")
    @Max(value = 100, message = "Відсоток не може перевищувати 100")
    private Integer discountPercent;

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCustSurname() { return custSurname; }
    public void setCustSurname(String custSurname) { this.custSurname = custSurname; }

    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }

    public String getCustPatronymic() { return custPatronymic; }
    public void setCustPatronymic(String custPatronymic) { this.custPatronymic = custPatronymic; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(custSurname).append(' ').append(custName);
        if (custPatronymic != null && !custPatronymic.isBlank()) {
            sb.append(' ').append(custPatronymic);
        }
        return sb.toString();
    }
}

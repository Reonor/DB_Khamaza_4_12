package ua.zlagoda.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * «Чек» (stored in table {@code receipt}). {@code sumTotal} already includes VAT;
 * {@code vat} is the VAT portion (sumTotal * 0.2 per the requirements).
 */
public class SaleCheck {

    private String checkNumber;
    private String idEmployee;
    private String cardNumber;
    private LocalDateTime printDate;
    private BigDecimal sumTotal;
    private BigDecimal vat;

    /** Display-only join fields. */
    private String employeeName;
    private String customerName;

    public String getCheckNumber() { return checkNumber; }
    public void setCheckNumber(String checkNumber) { this.checkNumber = checkNumber; }

    public String getIdEmployee() { return idEmployee; }
    public void setIdEmployee(String idEmployee) { this.idEmployee = idEmployee; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public LocalDateTime getPrintDate() { return printDate; }
    public void setPrintDate(LocalDateTime printDate) { this.printDate = printDate; }

    public BigDecimal getSumTotal() { return sumTotal; }
    public void setSumTotal(BigDecimal sumTotal) { this.sumTotal = sumTotal; }

    public BigDecimal getVat() { return vat; }
    public void setVat(BigDecimal vat) { this.vat = vat; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}

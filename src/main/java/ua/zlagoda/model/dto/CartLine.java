package ua.zlagoda.model.dto;

import java.math.BigDecimal;

/** One line in the cashier's working cart before the check is finalised. */
public class CartLine {

    private String upc;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    public CartLine() { }

    public CartLine(String upc, String productName, BigDecimal unitPrice, int quantity) {
        this.upc = upc;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getUpc() { return upc; }
    public void setUpc(String upc) { this.upc = upc; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

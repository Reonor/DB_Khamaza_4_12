package ua.zlagoda.model;

import java.math.BigDecimal;

/** «Продаж»: one product line inside a check. */
public class Sale {

    private String upc;
    private String checkNumber;
    private Integer productNumber;
    private BigDecimal sellingPrice;

    /** Display-only join field. */
    private String productName;

    public String getUpc() { return upc; }
    public void setUpc(String upc) { this.upc = upc; }

    public String getCheckNumber() { return checkNumber; }
    public void setCheckNumber(String checkNumber) { this.checkNumber = checkNumber; }

    public Integer getProductNumber() { return productNumber; }
    public void setProductNumber(Integer productNumber) { this.productNumber = productNumber; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getLineTotal() {
        if (sellingPrice == null || productNumber == null) return BigDecimal.ZERO;
        return sellingPrice.multiply(BigDecimal.valueOf(productNumber));
    }
}

package ua.zlagoda.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** «Товар у магазині». A regular item or its promotional counterpart. */
public class StoreProduct {

    @NotBlank
    @Size(max = 12)
    private String upc;

    /** UPC of the linked promotional row (nullable). */
    @Size(max = 12)
    private String upcProm;

    @NotNull(message = "Оберіть товар")
    private Integer idProduct;

    // Conditionally required: computed (base × 0.8) for a promotional item, and
    // validated in StoreProductService for a regular item. So no @NotNull here —
    // a promotional submission legitimately leaves this empty.
    @DecimalMin(value = "0.0", message = "Ціна не може бути від'ємною")
    private BigDecimal sellingPrice;

    @NotNull
    @Min(value = 0, message = "Кількість не може бути від'ємною")
    private Integer productsNumber;

    private boolean promotionalProduct;

    /** Display-only join fields. */
    private String productName;
    private String characteristics;
    private String categoryName;

    public String getUpc() { return upc; }
    public void setUpc(String upc) { this.upc = upc; }

    public String getUpcProm() { return upcProm; }
    public void setUpcProm(String upcProm) { this.upcProm = upcProm; }

    public Integer getIdProduct() { return idProduct; }
    public void setIdProduct(Integer idProduct) { this.idProduct = idProduct; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public Integer getProductsNumber() { return productsNumber; }
    public void setProductsNumber(Integer productsNumber) { this.productsNumber = productsNumber; }

    public boolean isPromotionalProduct() { return promotionalProduct; }
    public void setPromotionalProduct(boolean promotionalProduct) { this.promotionalProduct = promotionalProduct; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCharacteristics() { return characteristics; }
    public void setCharacteristics(String characteristics) { this.characteristics = characteristics; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}

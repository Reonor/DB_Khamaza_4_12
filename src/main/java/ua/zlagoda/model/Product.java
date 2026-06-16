package ua.zlagoda.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** «Товар». */
public class Product {

    private Integer idProduct;

    @NotNull(message = "Оберіть категорію")
    private Integer categoryNumber;

    @NotBlank
    @Size(max = 50)
    private String productName;

    @NotBlank
    @Size(max = 100)
    private String characteristics;

    /** Display-only, filled by joins. */
    private String categoryName;

    public Integer getIdProduct() { return idProduct; }
    public void setIdProduct(Integer idProduct) { this.idProduct = idProduct; }

    public Integer getCategoryNumber() { return categoryNumber; }
    public void setCategoryNumber(Integer categoryNumber) { this.categoryNumber = categoryNumber; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCharacteristics() { return characteristics; }
    public void setCharacteristics(String characteristics) { this.characteristics = characteristics; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}

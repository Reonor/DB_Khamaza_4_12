package ua.zlagoda.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** «Категорія» товарів. */
public class Category {

    private Integer categoryNumber;

    @NotBlank
    @Size(max = 50)
    private String categoryName;

    public Category() { }

    public Category(Integer categoryNumber, String categoryName) {
        this.categoryNumber = categoryNumber;
        this.categoryName = categoryName;
    }

    public Integer getCategoryNumber() { return categoryNumber; }
    public void setCategoryNumber(Integer categoryNumber) { this.categoryNumber = categoryNumber; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}

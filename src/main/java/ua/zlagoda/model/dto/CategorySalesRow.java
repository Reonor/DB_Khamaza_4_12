package ua.zlagoda.model.dto;

import java.math.BigDecimal;

/** One row of the «продаж за категоріями» grouped query result. */
public class CategorySalesRow {

    private String categoryName;
    private long totalUnits;
    private BigDecimal totalSum;

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public long getTotalUnits() { return totalUnits; }
    public void setTotalUnits(long totalUnits) { this.totalUnits = totalUnits; }

    public BigDecimal getTotalSum() { return totalSum; }
    public void setTotalSum(BigDecimal totalSum) { this.totalSum = totalSum; }
}

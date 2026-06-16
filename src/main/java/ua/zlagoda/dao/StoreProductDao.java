package ua.zlagoda.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.zlagoda.model.StoreProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class StoreProductDao {

    private final JdbcTemplate jdbc;

    public StoreProductDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<StoreProduct> MAPPER = (rs, n) -> {
        StoreProduct sp = new StoreProduct();
        sp.setUpc(rs.getString("upc"));
        sp.setUpcProm(rs.getString("upc_prom"));
        sp.setIdProduct(rs.getInt("id_product"));
        sp.setSellingPrice(rs.getBigDecimal("selling_price"));
        sp.setProductsNumber(rs.getInt("products_number"));
        sp.setPromotionalProduct(rs.getBoolean("promotional_product"));
        sp.setProductName(rs.getString("product_name"));
        sp.setCharacteristics(rs.getString("characteristics"));
        sp.setCategoryName(rs.getString("category_name"));
        return sp;
    };

    private static final String SELECT =
            "SELECT sp.upc, sp.upc_prom, sp.id_product, sp.selling_price, sp.products_number, " +
            "sp.promotional_product, p.product_name, p.characteristics, c.category_name " +
            "FROM store_product sp " +
            "JOIN product p ON p.id_product = sp.id_product " +
            "JOIN category c ON c.category_number = p.category_number ";

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "name"     -> "ORDER BY p.product_name";
            case "price"    -> "ORDER BY sp.selling_price";
            default          -> "ORDER BY sp.products_number DESC";   // quantity
        };
    }

    /** @param promo one of "all", "promo", "regular" */
    public List<StoreProduct> findAll(String promo, String sort) {
        String where = switch (promo == null ? "all" : promo) {
            case "promo"   -> "WHERE sp.promotional_product = TRUE ";
            case "regular" -> "WHERE sp.promotional_product = FALSE ";
            default         -> "";
        };
        return jdbc.query(SELECT + where + orderBy(sort), MAPPER);
    }

    public List<StoreProduct> searchByProductName(String name) {
        return jdbc.query(SELECT + "WHERE LOWER(p.product_name) LIKE LOWER(?) ORDER BY p.product_name",
                MAPPER, "%" + name + "%");
    }

    public Optional<StoreProduct> findByUpc(String upc) {
        List<StoreProduct> list = jdbc.query(SELECT + "WHERE sp.upc = ?", MAPPER, upc);
        return list.stream().findFirst();
    }

    /** UPCs of regular (non-promotional) items, used when linking a promo row. */
    public List<StoreProduct> findRegular() {
        return findAll("regular", "name");
    }

    public String nextUpc() {
        Long max = jdbc.queryForObject("SELECT MAX(CAST(upc AS BIGINT)) FROM store_product", Long.class);
        long base = (max == null ? 100000000000L : max) + 1;
        return String.format("%012d", base);
    }

    public void insert(StoreProduct sp) {
        jdbc.update("INSERT INTO store_product (upc, upc_prom, id_product, selling_price, " +
                "products_number, promotional_product) VALUES (?,?,?,?,?,?)",
                sp.getUpc(), emptyToNull(sp.getUpcProm()), sp.getIdProduct(), sp.getSellingPrice(),
                sp.getProductsNumber(), sp.isPromotionalProduct());
    }

    public void update(StoreProduct sp) {
        jdbc.update("UPDATE store_product SET upc_prom=?, id_product=?, selling_price=?, " +
                "products_number=?, promotional_product=? WHERE upc=?",
                emptyToNull(sp.getUpcProm()), sp.getIdProduct(), sp.getSellingPrice(),
                sp.getProductsNumber(), sp.isPromotionalProduct(), sp.getUpc());
    }

    /**
     * Receiving a new batch: re-prices the whole stock of this UPC to the new
     * price (переоцінка) and adds the received quantity.
     */
    public void receiveBatch(String upc, BigDecimal newPrice, int addedQty) {
        jdbc.update("UPDATE store_product SET selling_price=?, products_number = products_number + ? WHERE upc=?",
                newPrice, addedQty, upc);
    }

    public void decreaseStock(String upc, int qty) {
        jdbc.update("UPDATE store_product SET products_number = products_number - ? WHERE upc = ?", qty, upc);
    }

    public void delete(String upc) {
        jdbc.update("DELETE FROM store_product WHERE upc = ?", upc);
    }

    public int countSales(String upc) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM sale WHERE upc = ?", Integer.class, upc);
        return c == null ? 0 : c;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}

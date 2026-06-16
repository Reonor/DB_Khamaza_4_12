package ua.zlagoda.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.zlagoda.model.Product;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductDao {

    private final JdbcTemplate jdbc;

    public ProductDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Product> MAPPER = (rs, n) -> {
        Product p = new Product();
        p.setIdProduct(rs.getInt("id_product"));
        p.setCategoryNumber(rs.getInt("category_number"));
        p.setProductName(rs.getString("product_name"));
        p.setCharacteristics(rs.getString("characteristics"));
        p.setCategoryName(rs.getString("category_name"));
        return p;
    };

    private static final String SELECT =
            "SELECT p.id_product, p.category_number, p.product_name, p.characteristics, " +
            "c.category_name FROM product p JOIN category c ON c.category_number = p.category_number ";

    public List<Product> findAllOrderByName() {
        return jdbc.query(SELECT + "ORDER BY p.product_name", MAPPER);
    }

    public List<Product> findByCategoryOrderByName(int categoryNumber) {
        return jdbc.query(SELECT + "WHERE p.category_number = ? ORDER BY p.product_name", MAPPER, categoryNumber);
    }

    public List<Product> searchByName(String name) {
        return jdbc.query(SELECT + "WHERE LOWER(p.product_name) LIKE LOWER(?) ORDER BY p.product_name",
                MAPPER, "%" + name + "%");
    }

    public Optional<Product> findById(int id) {
        List<Product> list = jdbc.query(SELECT + "WHERE p.id_product = ?", MAPPER, id);
        return list.stream().findFirst();
    }

    public int nextId() {
        Integer max = jdbc.queryForObject("SELECT MAX(id_product) FROM product", Integer.class);
        return (max == null ? 0 : max) + 1;
    }

    public void insert(Product p) {
        jdbc.update("INSERT INTO product (id_product, category_number, product_name, characteristics) " +
                "VALUES (?,?,?,?)",
                p.getIdProduct(), p.getCategoryNumber(), p.getProductName(), p.getCharacteristics());
    }

    public void update(Product p) {
        jdbc.update("UPDATE product SET category_number=?, product_name=?, characteristics=? WHERE id_product=?",
                p.getCategoryNumber(), p.getProductName(), p.getCharacteristics(), p.getIdProduct());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM product WHERE id_product = ?", id);
    }

    public int countStoreProducts(int idProduct) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM store_product WHERE id_product = ?", Integer.class, idProduct);
        return c == null ? 0 : c;
    }
}

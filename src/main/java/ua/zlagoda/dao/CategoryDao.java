package ua.zlagoda.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.zlagoda.model.Category;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryDao {

    private final JdbcTemplate jdbc;

    public CategoryDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Category> MAPPER = (rs, n) ->
            new Category(rs.getInt("category_number"), rs.getString("category_name"));

    public List<Category> findAllOrderByName() {
        return jdbc.query("SELECT category_number, category_name FROM category ORDER BY category_name", MAPPER);
    }

    public Optional<Category> findById(int id) {
        List<Category> list = jdbc.query(
                "SELECT category_number, category_name FROM category WHERE category_number = ?", MAPPER, id);
        return list.stream().findFirst();
    }

    public int nextNumber() {
        Integer max = jdbc.queryForObject("SELECT MAX(category_number) FROM category", Integer.class);
        return (max == null ? 0 : max) + 1;
    }

    public void insert(Category c) {
        jdbc.update("INSERT INTO category (category_number, category_name) VALUES (?, ?)",
                c.getCategoryNumber(), c.getCategoryName());
    }

    public void update(Category c) {
        jdbc.update("UPDATE category SET category_name = ? WHERE category_number = ?",
                c.getCategoryName(), c.getCategoryNumber());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM category WHERE category_number = ?", id);
    }

    public int countProducts(int categoryNumber) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM product WHERE category_number = ?", Integer.class, categoryNumber);
        return c == null ? 0 : c;
    }
}

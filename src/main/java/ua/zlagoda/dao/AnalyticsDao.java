package ua.zlagoda.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.zlagoda.model.dto.CategorySalesRow;
import ua.zlagoda.model.dto.LoyalCustomerRow;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

/**
 * Two analytical multi-table queries used by the lab report.
 * Both are plain SQL (no ORM); one is parametric (period), the other uses
 * double negation (NOT EXISTS … NOT EXISTS — реляційне ділення).
 */
@Repository
public class AnalyticsDao {

    private final JdbcTemplate jdbc;

    public AnalyticsDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ---------------------------------------------------------------------
     *  ЗАПИТ 1 — багатотабличний (5 таблиць) з ГРУПУВАННЯМ, ПАРАМЕТРИЧНИЙ.
     *  Для кожної категорії: скільки одиниць продано та на яку суму
     *  за заданий період [from; to].
     * ------------------------------------------------------------------- */
    public static final String SQL_SALES_BY_CATEGORY = """
            SELECT c.category_name,
                   SUM(s.product_number)                    AS total_units,
                   SUM(s.product_number * s.selling_price)  AS total_sum
            FROM sale s
            JOIN store_product sp ON sp.upc = s.upc
            JOIN product       p  ON p.id_product = sp.id_product
            JOIN category      c  ON c.category_number = p.category_number
            JOIN receipt       r  ON r.check_number = s.check_number
            WHERE r.print_date >= ? AND r.print_date < ?
            GROUP BY c.category_name
            ORDER BY total_sum DESC
            """;

    private static final RowMapper<CategorySalesRow> CATEGORY_SALES_MAPPER = (rs, n) -> {
        CategorySalesRow row = new CategorySalesRow();
        row.setCategoryName(rs.getString("category_name"));
        row.setTotalUnits(rs.getLong("total_units"));
        row.setTotalSum(rs.getBigDecimal("total_sum"));
        return row;
    };

    public List<CategorySalesRow> salesByCategory(LocalDate from, LocalDate to) {
        // Верхня межа — початок наступного дня, щоб увесь день `to` входив у період.
        Timestamp start = Timestamp.valueOf(from.atStartOfDay());
        Timestamp end = Timestamp.valueOf(to.plusDays(1).atStartOfDay());
        return jdbc.query(SQL_SALES_BY_CATEGORY, CATEGORY_SALES_MAPPER, start, end);
    }

    /* ---------------------------------------------------------------------
     *  ЗАПИТ 2 — багатотабличний (6 таблиць) з ПОДВІЙНИМ ЗАПЕРЕЧЕННЯМ.
     *  Постійні клієнти, які купували товари ВСІХ наявних категорій:
     *  не існує категорії, товару з якої клієнт НЕ купував.
     * ------------------------------------------------------------------- */
    public static final String SQL_CUSTOMERS_ALL_CATEGORIES = """
            SELECT cc.card_number, cc.cust_surname, cc.cust_name, cc.phone_number
            FROM customer_card cc
            WHERE NOT EXISTS (
                    SELECT 1
                    FROM category c
                    WHERE NOT EXISTS (
                            SELECT 1
                            FROM receipt       r
                            JOIN sale          s  ON s.check_number = r.check_number
                            JOIN store_product sp ON sp.upc = s.upc
                            JOIN product       p  ON p.id_product = sp.id_product
                            WHERE r.card_number = cc.card_number
                              AND p.category_number = c.category_number
                    )
            )
            ORDER BY cc.cust_surname, cc.cust_name
            """;

    private static final RowMapper<LoyalCustomerRow> LOYAL_CUSTOMER_MAPPER = (rs, n) -> {
        LoyalCustomerRow row = new LoyalCustomerRow();
        row.setCardNumber(rs.getString("card_number"));
        row.setFullName(rs.getString("cust_surname") + " " + rs.getString("cust_name"));
        row.setPhoneNumber(rs.getString("phone_number"));
        return row;
    };

    public List<LoyalCustomerRow> customersBoughtAllCategories() {
        return jdbc.query(SQL_CUSTOMERS_ALL_CATEGORIES, LOYAL_CUSTOMER_MAPPER);
    }
}

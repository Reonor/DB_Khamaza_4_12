package ua.zlagoda.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.zlagoda.model.Sale;
import ua.zlagoda.model.SaleCheck;
import ua.zlagoda.model.dto.CheckView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data access for «Чек» (table {@code receipt}) and its «Продаж» lines (table {@code sale}).
 * Pure SQL only — no ORM. All period filters are inclusive of the whole end day.
 */
@Repository
public class CheckDao {

    private final JdbcTemplate jdbc;

    public CheckDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<SaleCheck> CHECK_MAPPER = (rs, n) -> {
        SaleCheck c = new SaleCheck();
        c.setCheckNumber(rs.getString("check_number"));
        c.setIdEmployee(rs.getString("id_employee"));
        c.setCardNumber(rs.getString("card_number"));
        c.setPrintDate(rs.getTimestamp("print_date").toLocalDateTime());
        c.setSumTotal(rs.getBigDecimal("sum_total"));
        c.setVat(rs.getBigDecimal("vat"));
        c.setEmployeeName(rs.getString("employee_name"));
        c.setCustomerName(rs.getString("customer_name"));
        return c;
    };

    private static final RowMapper<Sale> SALE_MAPPER = (rs, n) -> {
        Sale s = new Sale();
        s.setUpc(rs.getString("upc"));
        s.setCheckNumber(rs.getString("check_number"));
        s.setProductNumber(rs.getInt("product_number"));
        s.setSellingPrice(rs.getBigDecimal("selling_price"));
        s.setProductName(rs.getString("product_name"));
        return s;
    };

    private static final String SELECT_CHECK =
            "SELECT r.check_number, r.id_employee, r.card_number, r.print_date, r.sum_total, r.vat, " +
            "(e.empl_surname || ' ' || e.empl_name) AS employee_name, " +
            "(cc.cust_surname || ' ' || cc.cust_name) AS customer_name " +
            "FROM receipt r " +
            "JOIN employee e ON e.id_employee = r.id_employee " +
            "LEFT JOIN customer_card cc ON cc.card_number = r.card_number ";

    private static final String SELECT_SALE =
            "SELECT s.upc, s.check_number, s.product_number, s.selling_price, p.product_name " +
            "FROM sale s " +
            "JOIN store_product sp ON sp.upc = s.upc " +
            "JOIN product p ON p.id_product = sp.id_product ";

    /* ------------------------------------------------------------------ */
    /*  Writes                                                            */
    /* ------------------------------------------------------------------ */

    public String nextCheckNumber() {
        Integer max = jdbc.queryForObject(
                "SELECT COALESCE(MAX(CAST(SUBSTRING(check_number, 3) AS INT)), 0) FROM receipt",
                Integer.class);
        int next = (max == null ? 0 : max) + 1;
        return String.format("CH%03d", next);
    }

    public void insertReceipt(SaleCheck c) {
        jdbc.update("INSERT INTO receipt (check_number, id_employee, card_number, print_date, sum_total, vat) " +
                        "VALUES (?,?,?,?,?,?)",
                c.getCheckNumber(), c.getIdEmployee(), c.getCardNumber(),
                java.sql.Timestamp.valueOf(c.getPrintDate()), c.getSumTotal(), c.getVat());
    }

    public void insertSale(Sale s) {
        jdbc.update("INSERT INTO sale (upc, check_number, product_number, selling_price) VALUES (?,?,?,?)",
                s.getUpc(), s.getCheckNumber(), s.getProductNumber(), s.getSellingPrice());
    }

    public void delete(String checkNumber) {
        // sale rows are removed by ON DELETE CASCADE
        jdbc.update("DELETE FROM receipt WHERE check_number = ?", checkNumber);
    }

    /* ------------------------------------------------------------------ */
    /*  Reads                                                             */
    /* ------------------------------------------------------------------ */

    public Optional<SaleCheck> findById(String checkNumber) {
        List<SaleCheck> list = jdbc.query(SELECT_CHECK + "WHERE r.check_number = ?", CHECK_MAPPER, checkNumber);
        return list.stream().findFirst();
    }

    public List<Sale> findLines(String checkNumber) {
        return jdbc.query(SELECT_SALE + "WHERE s.check_number = ? ORDER BY p.product_name",
                SALE_MAPPER, checkNumber);
    }

    /** Full receipt with its lines, or empty if the receipt does not exist. */
    public Optional<CheckView> findView(String checkNumber) {
        return findById(checkNumber).map(c -> new CheckView(c, findLines(checkNumber)));
    }

    /** All receipts of one cashier within a period (inclusive). */
    public List<SaleCheck> findByEmployeeAndPeriod(String idEmployee, LocalDate from, LocalDate to) {
        return jdbc.query(SELECT_CHECK +
                        "WHERE r.id_employee = ? AND r.print_date >= ? AND r.print_date < ? " +
                        "ORDER BY r.print_date DESC",
                CHECK_MAPPER, idEmployee, startOf(from), endOf(to));
    }

    /** All receipts by all cashiers within a period (inclusive). */
    public List<SaleCheck> findByPeriod(LocalDate from, LocalDate to) {
        return jdbc.query(SELECT_CHECK +
                        "WHERE r.print_date >= ? AND r.print_date < ? " +
                        "ORDER BY r.print_date DESC",
                CHECK_MAPPER, startOf(from), endOf(to));
    }

    /** Receipts created by one cashier on a single day (used for "my checks today"). */
    public List<SaleCheck> findByEmployeeOnDay(String idEmployee, LocalDate day) {
        return findByEmployeeAndPeriod(idEmployee, day, day);
    }

    /* ------------------------------------------------------------------ */
    /*  Aggregates (manager reports 19-21)                                 */
    /* ------------------------------------------------------------------ */

    /** Total money of all checks of one cashier in a period. */
    public BigDecimal totalSumByEmployee(String idEmployee, LocalDate from, LocalDate to) {
        BigDecimal v = jdbc.queryForObject(
                "SELECT COALESCE(SUM(sum_total), 0) FROM receipt " +
                        "WHERE id_employee = ? AND print_date >= ? AND print_date < ?",
                BigDecimal.class, idEmployee, startOf(from), endOf(to));
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Total money of all checks of all cashiers in a period. */
    public BigDecimal totalSumAll(LocalDate from, LocalDate to) {
        BigDecimal v = jdbc.queryForObject(
                "SELECT COALESCE(SUM(sum_total), 0) FROM receipt " +
                        "WHERE print_date >= ? AND print_date < ?",
                BigDecimal.class, startOf(from), endOf(to));
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Total quantity of one product (by UPC) sold in a period. */
    public long totalQuantityOfProduct(String upc, LocalDate from, LocalDate to) {
        Long v = jdbc.queryForObject(
                "SELECT COALESCE(SUM(s.product_number), 0) FROM sale s " +
                        "JOIN receipt r ON r.check_number = s.check_number " +
                        "WHERE s.upc = ? AND r.print_date >= ? AND r.print_date < ?",
                Long.class, upc, startOf(from), endOf(to));
        return v == null ? 0L : v;
    }

    private static java.sql.Timestamp startOf(LocalDate d) {
        return java.sql.Timestamp.valueOf(d.atStartOfDay());
    }

    private static java.sql.Timestamp endOf(LocalDate d) {
        // exclusive upper bound = start of the next day, so the whole end day is included
        return java.sql.Timestamp.valueOf(d.plusDays(1).atStartOfDay());
    }
}

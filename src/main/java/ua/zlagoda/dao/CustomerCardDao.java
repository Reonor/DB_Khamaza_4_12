package ua.zlagoda.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.zlagoda.model.CustomerCard;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerCardDao {

    private final JdbcTemplate jdbc;

    public CustomerCardDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<CustomerCard> MAPPER = (rs, n) -> {
        CustomerCard c = new CustomerCard();
        c.setCardNumber(rs.getString("card_number"));
        c.setCustSurname(rs.getString("cust_surname"));
        c.setCustName(rs.getString("cust_name"));
        c.setCustPatronymic(rs.getString("cust_patronymic"));
        c.setPhoneNumber(rs.getString("phone_number"));
        c.setCity(rs.getString("city"));
        c.setStreet(rs.getString("street"));
        c.setZipCode(rs.getString("zip_code"));
        c.setDiscountPercent(rs.getInt("discount_percent"));
        return c;
    };

    private static final String SELECT =
            "SELECT card_number, cust_surname, cust_name, cust_patronymic, phone_number, city, street, " +
            "zip_code, discount_percent FROM customer_card ";

    public List<CustomerCard> findAllOrderBySurname() {
        return jdbc.query(SELECT + "ORDER BY cust_surname, cust_name", MAPPER);
    }

    public List<CustomerCard> findByPercentOrderBySurname(int percent) {
        return jdbc.query(SELECT + "WHERE discount_percent = ? ORDER BY cust_surname, cust_name", MAPPER, percent);
    }

    public List<CustomerCard> searchBySurname(String surname) {
        return jdbc.query(SELECT + "WHERE LOWER(cust_surname) LIKE LOWER(?) ORDER BY cust_surname, cust_name",
                MAPPER, "%" + surname + "%");
    }

    public Optional<CustomerCard> findById(String cardNumber) {
        List<CustomerCard> list = jdbc.query(SELECT + "WHERE card_number = ?", MAPPER, cardNumber);
        return list.stream().findFirst();
    }

    public String nextCardNumber() {
        Integer max = jdbc.queryForObject(
                "SELECT MAX(CAST(SUBSTRING(card_number, 2) AS INT)) FROM customer_card", Integer.class);
        return String.format("C%03d", (max == null ? 0 : max) + 1);
    }

    public void insert(CustomerCard c) {
        jdbc.update("INSERT INTO customer_card (card_number, cust_surname, cust_name, cust_patronymic, " +
                "phone_number, city, street, zip_code, discount_percent) VALUES (?,?,?,?,?,?,?,?,?)",
                c.getCardNumber(), c.getCustSurname(), c.getCustName(), c.getCustPatronymic(),
                c.getPhoneNumber(), c.getCity(), c.getStreet(), c.getZipCode(), c.getDiscountPercent());
    }

    public void update(CustomerCard c) {
        jdbc.update("UPDATE customer_card SET cust_surname=?, cust_name=?, cust_patronymic=?, phone_number=?, " +
                "city=?, street=?, zip_code=?, discount_percent=? WHERE card_number=?",
                c.getCustSurname(), c.getCustName(), c.getCustPatronymic(), c.getPhoneNumber(),
                c.getCity(), c.getStreet(), c.getZipCode(), c.getDiscountPercent(), c.getCardNumber());
    }

    public void delete(String cardNumber) {
        jdbc.update("DELETE FROM customer_card WHERE card_number = ?", cardNumber);
    }

    public int countChecks(String cardNumber) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM receipt WHERE card_number = ?", Integer.class, cardNumber);
        return c == null ? 0 : c;
    }
}

package ua.zlagoda.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.zlagoda.model.Employee;

import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeDao {

    private final JdbcTemplate jdbc;

    public EmployeeDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Employee> MAPPER = (rs, n) -> {
        Employee e = new Employee();
        e.setIdEmployee(rs.getString("id_employee"));
        e.setEmplSurname(rs.getString("empl_surname"));
        e.setEmplName(rs.getString("empl_name"));
        e.setEmplPatronymic(rs.getString("empl_patronymic"));
        e.setEmplRole(rs.getString("empl_role"));
        e.setSalary(rs.getBigDecimal("salary"));
        e.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        e.setDateOfStart(rs.getDate("date_of_start").toLocalDate());
        e.setPhoneNumber(rs.getString("phone_number"));
        e.setCity(rs.getString("city"));
        e.setStreet(rs.getString("street"));
        e.setZipCode(rs.getString("zip_code"));
        e.setLogin(rs.getString("login"));
        e.setPasswordHash(rs.getString("password_hash"));
        return e;
    };

    private static final String SELECT =
            "SELECT id_employee, empl_surname, empl_name, empl_patronymic, empl_role, salary, " +
            "date_of_birth, date_of_start, phone_number, city, street, zip_code, login, password_hash " +
            "FROM employee ";

    public List<Employee> findAllOrderBySurname() {
        return jdbc.query(SELECT + "ORDER BY empl_surname, empl_name", MAPPER);
    }

    public List<Employee> findByRoleOrderBySurname(String role) {
        return jdbc.query(SELECT + "WHERE empl_role = ? ORDER BY empl_surname, empl_name", MAPPER, role);
    }

    public List<Employee> findBySurname(String surname) {
        return jdbc.query(SELECT + "WHERE LOWER(empl_surname) LIKE LOWER(?) ORDER BY empl_surname, empl_name",
                MAPPER, "%" + surname + "%");
    }

    public Optional<Employee> findById(String id) {
        List<Employee> list = jdbc.query(SELECT + "WHERE id_employee = ?", MAPPER, id);
        return list.stream().findFirst();
    }

    public Optional<Employee> findByLogin(String login) {
        List<Employee> list = jdbc.query(SELECT + "WHERE login = ?", MAPPER, login);
        return list.stream().findFirst();
    }

    public boolean loginExists(String login, String exceptId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM employee WHERE login = ? AND id_employee <> ?",
                Integer.class, login, exceptId == null ? "" : exceptId);
        return c != null && c > 0;
    }

    public String nextId() {
        Integer max = jdbc.queryForObject(
                "SELECT MAX(CAST(SUBSTRING(id_employee, 2) AS INT)) FROM employee", Integer.class);
        return String.format("E%03d", (max == null ? 0 : max) + 1);
    }

    public void insert(Employee e) {
        jdbc.update("INSERT INTO employee (id_employee, empl_surname, empl_name, empl_patronymic, " +
                "empl_role, salary, date_of_birth, date_of_start, phone_number, city, street, zip_code, " +
                "login, password_hash) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                e.getIdEmployee(), e.getEmplSurname(), e.getEmplName(), e.getEmplPatronymic(),
                e.getEmplRole(), e.getSalary(), e.getDateOfBirth(), e.getDateOfStart(),
                e.getPhoneNumber(), e.getCity(), e.getStreet(), e.getZipCode(),
                e.getLogin(), e.getPasswordHash());
    }

    /** Updates everything except the password hash. */
    public void update(Employee e) {
        jdbc.update("UPDATE employee SET empl_surname=?, empl_name=?, empl_patronymic=?, empl_role=?, " +
                "salary=?, date_of_birth=?, date_of_start=?, phone_number=?, city=?, street=?, zip_code=?, " +
                "login=? WHERE id_employee=?",
                e.getEmplSurname(), e.getEmplName(), e.getEmplPatronymic(), e.getEmplRole(),
                e.getSalary(), e.getDateOfBirth(), e.getDateOfStart(), e.getPhoneNumber(),
                e.getCity(), e.getStreet(), e.getZipCode(), e.getLogin(), e.getIdEmployee());
    }

    public void updatePassword(String id, String passwordHash) {
        jdbc.update("UPDATE employee SET password_hash=? WHERE id_employee=?", passwordHash, id);
    }

    public void delete(String id) {
        jdbc.update("DELETE FROM employee WHERE id_employee = ?", id);
    }
}

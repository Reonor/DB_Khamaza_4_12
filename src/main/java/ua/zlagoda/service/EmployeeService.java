package ua.zlagoda.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.zlagoda.dao.EmployeeDao;
import ua.zlagoda.model.Employee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business rules for employees: password encryption (never stored in plain text),
 * the «вік ≥ 18» integrity rule, and automatic id/login generation.
 */
@Service
public class EmployeeService {

    private final EmployeeDao dao;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeDao dao, PasswordEncoder passwordEncoder) {
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Employee> findAll() { return dao.findAllOrderBySurname(); }

    public List<Employee> findCashiers() { return dao.findByRoleOrderBySurname("касир"); }

    public List<Employee> findBySurname(String surname) { return dao.findBySurname(surname); }

    public Optional<Employee> findById(String id) { return dao.findById(id); }

    @Transactional
    public Employee create(Employee e) {
        validateAge(e.getDateOfBirth());
        requireLogin(e, true);
        e.setIdEmployee(dao.nextId());
        e.setPasswordHash(passwordEncoder.encode(e.getPassword()));
        dao.insert(e);
        return e;
    }

    @Transactional
    public void update(Employee e) {
        validateAge(e.getDateOfBirth());
        requireLogin(e, false);
        dao.update(e);
        // password changed only when a new one is supplied on the edit form
        if (e.getPassword() != null && !e.getPassword().isBlank()) {
            dao.updatePassword(e.getIdEmployee(), passwordEncoder.encode(e.getPassword()));
        }
    }

    @Transactional
    public void delete(String id) { dao.delete(id); }

    private void requireLogin(Employee e, boolean creating) {
        if (e.getLogin() == null || e.getLogin().isBlank()) {
            throw new IllegalArgumentException("Логін є обов'язковим.");
        }
        if (creating && (e.getPassword() == null || e.getPassword().isBlank())) {
            throw new IllegalArgumentException("Пароль є обов'язковим для нового працівника.");
        }
        String exceptId = creating ? null : e.getIdEmployee();
        if (dao.loginExists(e.getLogin(), exceptId)) {
            throw new IllegalArgumentException("Логін «" + e.getLogin() + "» вже зайнятий.");
        }
    }

    /** Семантичне обмеження: вік працівника не може бути меншим за 18 років. */
    private void validateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Дата народження є обов'язковою.");
        }
        if (dateOfBirth.isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Працівник повинен бути не молодшим за 18 років.");
        }
    }
}

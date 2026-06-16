package ua.zlagoda.service;

import org.springframework.stereotype.Service;
import ua.zlagoda.dao.CustomerCardDao;
import ua.zlagoda.model.CustomerCard;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerCardService {

    private final CustomerCardDao dao;

    public CustomerCardService(CustomerCardDao dao) {
        this.dao = dao;
    }

    public List<CustomerCard> findAll() { return dao.findAllOrderBySurname(); }

    public List<CustomerCard> findByPercent(int percent) { return dao.findByPercentOrderBySurname(percent); }

    public List<CustomerCard> searchBySurname(String surname) { return dao.searchBySurname(surname); }

    public Optional<CustomerCard> findById(String cardNumber) { return dao.findById(cardNumber); }

    public CustomerCard create(CustomerCard c) {
        c.setCardNumber(dao.nextCardNumber());
        dao.insert(c);
        return c;
    }

    public void update(CustomerCard c) { dao.update(c); }

    public void delete(String cardNumber) {
        if (dao.countChecks(cardNumber) > 0) {
            throw new IllegalStateException("Неможливо видалити карту: вона використана в чеках.");
        }
        dao.delete(cardNumber);
    }
}

package ua.zlagoda.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.zlagoda.dao.AnalyticsDao;

import java.time.LocalDate;

/**
 * «Аналітика» — сторінки для двох складних запитів (для лабораторної роботи).
 * Доступні лише менеджеру. Результат відображається у графічному інтерфейсі.
 */
@Controller
@RequestMapping("/analytics")
@PreAuthorize("hasRole('MANAGER')")
public class AnalyticsController {

    private final AnalyticsDao dao;

    public AnalyticsController(AnalyticsDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public String hub() {
        return "analytics/hub";
    }

    /** ЗАПИТ 1 (параметричний, з групуванням): продаж за категоріями за період. */
    @GetMapping("/sales-by-category")
    public String salesByCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model) {
        if (from != null && to != null) {
            model.addAttribute("rows", dao.salesByCategory(from, to));
            model.addAttribute("executed", true);
        }
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("sql", AnalyticsDao.SQL_SALES_BY_CATEGORY);
        return "analytics/sales-by-category";
    }

    /** ЗАПИТ 2 (подвійне заперечення): клієнти, що купували товари всіх категорій. */
    @GetMapping("/loyal-customers")
    public String loyalCustomers(Model model) {
        model.addAttribute("rows", dao.customersBoughtAllCategories());
        model.addAttribute("executed", true);
        model.addAttribute("sql", AnalyticsDao.SQL_CUSTOMERS_ALL_CATEGORIES);
        return "analytics/loyal-customers";
    }
}

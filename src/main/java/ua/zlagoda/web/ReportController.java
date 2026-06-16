package ua.zlagoda.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.zlagoda.service.*;

import java.time.LocalDate;

/**
 * Manager-only printable reports (requirement: друк звітів).
 * Each report renders a preview page with a "Друкувати" button that calls
 * {@code window.print()}; print CSS gives a full-width table with running
 * header/footer (колонтитули) and no page numbers/addresses.
 */
@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('MANAGER')")
public class ReportController {

    private final EmployeeService employeeService;
    private final CustomerCardService customerCardService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final StoreProductService storeProductService;
    private final CheckService checkService;

    public ReportController(EmployeeService employeeService,
                            CustomerCardService customerCardService,
                            CategoryService categoryService,
                            ProductService productService,
                            StoreProductService storeProductService,
                            CheckService checkService) {
        this.employeeService = employeeService;
        this.customerCardService = customerCardService;
        this.categoryService = categoryService;
        this.productService = productService;
        this.storeProductService = storeProductService;
        this.checkService = checkService;
    }

    @GetMapping
    public String hub(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "reports/hub";
    }

    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("title", "Звіт: Працівники");
        model.addAttribute("employees", employeeService.findAll());
        return "reports/employees";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("title", "Звіт: Постійні клієнти");
        model.addAttribute("customers", customerCardService.findAll());
        return "reports/customers";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("title", "Звіт: Категорії");
        model.addAttribute("categories", categoryService.findAll());
        return "reports/categories";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("title", "Звіт: Товари");
        model.addAttribute("products", productService.findAll());
        return "reports/products";
    }

    @GetMapping("/store-products")
    public String storeProducts(Model model) {
        model.addAttribute("title", "Звіт: Товари у магазині");
        model.addAttribute("storeProducts", storeProductService.findAll("all", "name"));
        return "reports/store-products";
    }

    @GetMapping("/checks")
    public String checks(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         @RequestParam(required = false) String employeeId,
                         Model model) {
        LocalDate f = (from != null) ? from : LocalDate.now().minusDays(30);
        LocalDate t = (to != null) ? to : LocalDate.now();
        if (employeeId != null && !employeeId.isBlank()) {
            model.addAttribute("checks", checkService.byEmployeeAndPeriod(employeeId, f, t));
        } else {
            model.addAttribute("checks", checkService.byPeriod(f, t));
        }
        model.addAttribute("title", "Звіт: Чеки");
        model.addAttribute("from", f);
        model.addAttribute("to", t);
        return "reports/checks";
    }

    /** Reports 19, 20, 21 combined: totals over a period. */
    @GetMapping("/sales-summary")
    public String salesSummary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               @RequestParam(required = false) String employeeId,
                               @RequestParam(required = false) String upc,
                               Model model) {
        model.addAttribute("title", "Звіт: Підсумки продажів");
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("totalAll", checkService.totalSumAll(from, to));

        if (employeeId != null && !employeeId.isBlank()) {
            model.addAttribute("employee", employeeService.findById(employeeId).orElse(null));
            model.addAttribute("totalByEmployee", checkService.totalSumByEmployee(employeeId, from, to));
        }
        if (upc != null && !upc.isBlank()) {
            model.addAttribute("storeProduct", storeProductService.findByUpc(upc).orElse(null));
            model.addAttribute("productQty", checkService.totalQuantityOfProduct(upc, from, to));
        }
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("storeProducts", storeProductService.findAll("all", "name"));
        return "reports/sales-summary";
    }
}

package ua.zlagoda.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.zlagoda.model.dto.Cart;
import ua.zlagoda.security.SecurityUser;
import ua.zlagoda.service.CheckService;
import ua.zlagoda.service.CustomerCardService;
import ua.zlagoda.service.StoreProductService;

import java.time.LocalDate;

@Controller
@RequestMapping("/checks")
public class CheckController {

    private final CheckService checkService;
    private final StoreProductService storeProductService;
    private final CustomerCardService customerCardService;
    private final ua.zlagoda.service.EmployeeService employeeService;
    private final Cart cart;

    public CheckController(CheckService checkService,
                           StoreProductService storeProductService,
                           CustomerCardService customerCardService,
                           ua.zlagoda.service.EmployeeService employeeService,
                           Cart cart) {
        this.checkService = checkService;
        this.storeProductService = storeProductService;
        this.customerCardService = customerCardService;
        this.employeeService = employeeService;
        this.cart = cart;
    }

    /* ------------------------------------------------------------------ */
    /*  Listing (cashier 9,10,11 / manager 17,18)                          */
    /* ------------------------------------------------------------------ */

    @GetMapping
    public String list(@AuthenticationPrincipal SecurityUser user,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) String employeeId,
                       Model model) {

        boolean manager = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        if (manager) {
            LocalDate f = (from != null) ? from : LocalDate.now().minusDays(30);
            LocalDate t = (to != null) ? to : LocalDate.now();
            if (employeeId != null && !employeeId.isBlank()) {
                model.addAttribute("checks", checkService.byEmployeeAndPeriod(employeeId, f, t));
            } else {
                model.addAttribute("checks", checkService.byPeriod(f, t));
            }
            model.addAttribute("from", f);
            model.addAttribute("to", t);
            model.addAttribute("employeeId", employeeId);
            model.addAttribute("cashiers", employeeService.findCashiers());
        } else {
            // cashier sees only their own checks
            if (from != null && to != null) {
                model.addAttribute("checks",
                        checkService.byEmployeeAndPeriod(user.getIdEmployee(), from, to));
            } else {
                LocalDate today = LocalDate.now();
                model.addAttribute("checks",
                        checkService.byEmployeeOnDay(user.getIdEmployee(), today));
                from = today;
                to = today;
            }
            model.addAttribute("from", from);
            model.addAttribute("to", to);
        }
        model.addAttribute("manager", manager);
        return "checks/list";
    }

    @GetMapping("/{number}")
    public String view(@PathVariable String number, Model model) {
        model.addAttribute("view", checkService.view(number).orElseThrow());
        return "checks/view";
    }

    /* ------------------------------------------------------------------ */
    /*  Sale terminal (cashier function 7) — session cart                  */
    /* ------------------------------------------------------------------ */

    @GetMapping("/new")
    @PreAuthorize("hasRole('CASHIER')")
    public String terminal(Model model) {
        model.addAttribute("cart", cart);
        model.addAttribute("products", storeProductService.findAll("all", "name"));
        model.addAttribute("cards", customerCardService.findAll());
        return "checks/new";
    }

    @PostMapping("/cart/add")
    @PreAuthorize("hasRole('CASHIER')")
    public String addToCart(@RequestParam String upc,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes ra) {
        try {
            checkService.addToCart(cart, upc, quantity);
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/checks/new";
    }

    @PostMapping("/cart/remove")
    @PreAuthorize("hasRole('CASHIER')")
    public String removeFromCart(@RequestParam String upc) {
        cart.remove(upc);
        return "redirect:/checks/new";
    }

    @PostMapping("/cart/card")
    @PreAuthorize("hasRole('CASHIER')")
    public String setCard(@RequestParam(required = false) String cardNumber) {
        cart.setCardNumber(cardNumber);
        return "redirect:/checks/new";
    }

    @PostMapping("/cart/clear")
    @PreAuthorize("hasRole('CASHIER')")
    public String clearCart() {
        cart.clear();
        return "redirect:/checks/new";
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CASHIER')")
    public String checkout(@AuthenticationPrincipal SecurityUser user, RedirectAttributes ra) {
        try {
            String number = checkService.checkout(cart, user.getIdEmployee());
            ra.addFlashAttribute("success", "Чек " + number + " створено.");
            return "redirect:/checks/" + number;
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/checks/new";
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Delete (manager only)                                              */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{number}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String delete(@PathVariable String number, RedirectAttributes ra) {
        checkService.delete(number);
        ra.addFlashAttribute("success", "Чек видалено.");
        return "redirect:/checks";
    }
}

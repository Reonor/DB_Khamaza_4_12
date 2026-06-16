package ua.zlagoda.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.zlagoda.model.CustomerCard;
import ua.zlagoda.service.CustomerCardService;

@Controller
@RequestMapping("/customers")
public class CustomerCardController {

    private final CustomerCardService service;

    public CustomerCardController(CustomerCardService service) {
        this.service = service;
    }

    /** Manager reports 7, 12 / cashier functions 3, 6: list, filter by percent, search by surname. */
    @GetMapping
    public String list(@RequestParam(required = false) Integer percent,
                       @RequestParam(required = false) String surname,
                       Model model) {
        if (surname != null && !surname.isBlank()) {
            model.addAttribute("customers", service.searchBySurname(surname));
        } else if (percent != null) {
            model.addAttribute("customers", service.findByPercent(percent));
        } else {
            model.addAttribute("customers", service.findAll());
        }
        model.addAttribute("percent", percent);
        model.addAttribute("surname", surname);
        return "customers/list";
    }

    // Add & edit are allowed for both manager and cashier (requirement: оновлення картки — обидві ролі).
    @GetMapping("/new")
    public String addForm(Model model) {
        CustomerCard c = new CustomerCard();
        c.setCardNumber("NEW"); // placeholder; real Cxxx number generated on save
        model.addAttribute("customer", c);
        model.addAttribute("editing", false);
        return "customers/form";
    }

    @GetMapping("/{cardNumber}/edit")
    public String editForm(@PathVariable String cardNumber, Model model) {
        model.addAttribute("customer", service.findById(cardNumber).orElseThrow());
        model.addAttribute("editing", true);
        return "customers/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("customer") CustomerCard customer,
                       BindingResult binding,
                       @RequestParam boolean editing,
                       Model model,
                       RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("editing", editing);
            return "customers/form";
        }
        if (editing) {
            service.update(customer);
        } else {
            service.create(customer);
        }
        ra.addFlashAttribute("success", "Картку клієнта збережено.");
        return "redirect:/customers";
    }

    @PostMapping("/{cardNumber}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String delete(@PathVariable String cardNumber, RedirectAttributes ra) {
        try {
            service.delete(cardNumber);
            ra.addFlashAttribute("success", "Картку клієнта видалено.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customers";
    }
}

package ua.zlagoda.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.zlagoda.model.Employee;
import ua.zlagoda.security.SecurityUser;
import ua.zlagoda.service.EmployeeService;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    /** Manager reports 5, 6, 11: list all / cashiers only / search by surname. */
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public String list(@RequestParam(required = false) String role,
                       @RequestParam(required = false) String surname,
                       Model model) {
        if (surname != null && !surname.isBlank()) {
            model.addAttribute("employees", service.findBySurname(surname));
        } else if ("касир".equals(role)) {
            model.addAttribute("employees", service.findCashiers());
        } else {
            model.addAttribute("employees", service.findAll());
        }
        model.addAttribute("role", role);
        model.addAttribute("surname", surname);
        return "employees/list";
    }

    /** Cashier function 15 (and manager): full information about oneself. */
    @GetMapping("/me")
    public String me(@AuthenticationPrincipal SecurityUser user, Model model) {
        model.addAttribute("employee", service.findById(user.getIdEmployee()).orElseThrow());
        return "employees/me";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String addForm(Model model) {
        Employee e = new Employee();
        e.setIdEmployee("NEW"); // placeholder; real Exxx id is generated on save
        model.addAttribute("employee", e);
        model.addAttribute("editing", false);
        return "employees/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('MANAGER')")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("employee", service.findById(id).orElseThrow());
        model.addAttribute("editing", true);
        return "employees/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('MANAGER')")
    public String save(@Valid @ModelAttribute("employee") Employee employee,
                       BindingResult binding,
                       @RequestParam boolean editing,
                       Model model,
                       RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("editing", editing);
            return "employees/form";
        }
        try {
            if (editing) {
                service.update(employee);
            } else {
                service.create(employee);
            }
        } catch (RuntimeException ex) {
            model.addAttribute("editing", editing);
            model.addAttribute("formError", ex.getMessage());
            return "employees/form";
        }
        ra.addFlashAttribute("success", "Дані працівника збережено.");
        return "redirect:/employees";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String delete(@PathVariable String id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Працівника видалено.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", "Не вдалося видалити працівника: " + ex.getMessage());
        }
        return "redirect:/employees";
    }
}

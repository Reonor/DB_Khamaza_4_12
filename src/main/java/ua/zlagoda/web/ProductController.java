package ua.zlagoda.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.zlagoda.model.Product;
import ua.zlagoda.service.CategoryService;
import ua.zlagoda.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;
    private final CategoryService categoryService;

    public ProductController(ProductService service, CategoryService categoryService) {
        this.service = service;
        this.categoryService = categoryService;
    }

    /** Manager reports 9, 13 / cashier functions 1, 4, 5: list, filter by category, search by name. */
    @GetMapping
    public String list(@RequestParam(required = false) Integer categoryNumber,
                       @RequestParam(required = false) String name,
                       Model model) {
        if (name != null && !name.isBlank()) {
            model.addAttribute("products", service.searchByName(name));
        } else if (categoryNumber != null) {
            model.addAttribute("products", service.findByCategory(categoryNumber));
        } else {
            model.addAttribute("products", service.findAll());
        }
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("categoryNumber", categoryNumber);
        model.addAttribute("name", name);
        return "products/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String addForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("editing", false);
        return "products/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('MANAGER')")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("product", service.findById(id).orElseThrow());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("editing", true);
        return "products/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('MANAGER')")
    public String save(@Valid @ModelAttribute("product") Product product,
                       BindingResult binding,
                       @RequestParam boolean editing,
                       Model model,
                       RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("editing", editing);
            return "products/form";
        }
        if (editing) {
            service.update(product);
        } else {
            service.create(product);
        }
        ra.addFlashAttribute("success", "Товар збережено.");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Товар видалено.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/products";
    }
}

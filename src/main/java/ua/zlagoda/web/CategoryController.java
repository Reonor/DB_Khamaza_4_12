package ua.zlagoda.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.zlagoda.model.Category;
import ua.zlagoda.service.CategoryService;

@Controller
@RequestMapping("/categories")
@PreAuthorize("hasRole('MANAGER')")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    /** Manager report 8: all categories sorted by name. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", service.findAll());
        return "categories/list";
    }

    @GetMapping("/new")
    public String addForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("editing", false);
        return "categories/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("category", service.findById(id).orElseThrow());
        model.addAttribute("editing", true);
        return "categories/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") Category category,
                       BindingResult binding,
                       @RequestParam boolean editing,
                       Model model,
                       RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("editing", editing);
            return "categories/form";
        }
        if (editing) {
            service.update(category);
        } else {
            service.create(category);
        }
        ra.addFlashAttribute("success", "Категорію збережено.");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Категорію видалено.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/categories";
    }
}

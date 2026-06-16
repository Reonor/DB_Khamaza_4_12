package ua.zlagoda.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.zlagoda.model.StoreProduct;
import ua.zlagoda.service.ProductService;
import ua.zlagoda.service.StoreProductService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/store-products")
public class StoreProductController {

    private final StoreProductService service;
    private final ProductService productService;

    public StoreProductController(StoreProductService service, ProductService productService) {
        this.service = service;
        this.productService = productService;
    }

    /**
     * Manager reports 10, 14, 15, 16 / cashier functions 2, 12, 13, 14.
     * @param promo "all" | "promo" | "regular"; @param sort "quantity" | "name" | "price".
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "all") String promo,
                       @RequestParam(defaultValue = "quantity") String sort,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String upc,
                       Model model) {
        if (upc != null && !upc.isBlank()) {
            model.addAttribute("storeProducts",
                    service.findByUpc(upc).map(java.util.List::of).orElse(java.util.List.of()));
        } else if (name != null && !name.isBlank()) {
            model.addAttribute("storeProducts", service.searchByProductName(name));
        } else {
            model.addAttribute("storeProducts", service.findAll(promo, sort));
        }
        model.addAttribute("promo", promo);
        model.addAttribute("sort", sort);
        model.addAttribute("name", name);
        model.addAttribute("upc", upc);
        return "storeproducts/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String addForm(Model model) {
        StoreProduct sp = new StoreProduct();
        sp.setUpc("NEW"); // placeholder; real 12-digit UPC generated on save
        sp.setPromotionalProduct(false);
        model.addAttribute("storeProduct", sp);
        model.addAttribute("products", productService.findAll());
        model.addAttribute("regulars", service.findRegular());
        model.addAttribute("editing", false);
        return "storeproducts/form";
    }

    @GetMapping("/{upc}/edit")
    @PreAuthorize("hasRole('MANAGER')")
    public String editForm(@PathVariable String upc, Model model) {
        model.addAttribute("storeProduct", service.findByUpc(upc).orElseThrow());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("regulars", service.findRegular());
        model.addAttribute("editing", true);
        return "storeproducts/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('MANAGER')")
    public String save(@Valid @ModelAttribute("storeProduct") StoreProduct storeProduct,
                       BindingResult binding,
                       @RequestParam boolean editing,
                       Model model,
                       RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("products", productService.findAll());
            model.addAttribute("regulars", service.findRegular());
            model.addAttribute("editing", editing);
            return "storeproducts/form";
        }
        try {
            if (editing) {
                service.update(storeProduct);
            } else {
                service.create(storeProduct);
            }
        } catch (RuntimeException ex) {
            model.addAttribute("products", productService.findAll());
            model.addAttribute("regulars", service.findRegular());
            model.addAttribute("editing", editing);
            model.addAttribute("formError", ex.getMessage());
            return "storeproducts/form";
        }
        ra.addFlashAttribute("success", "Товар у магазині збережено.");
        return "redirect:/store-products";
    }

    /** Receiving a new batch (переоцінка): re-prices whole stock and adds quantity. */
    @GetMapping("/{upc}/receive")
    @PreAuthorize("hasRole('MANAGER')")
    public String receiveForm(@PathVariable String upc, Model model) {
        model.addAttribute("storeProduct", service.findByUpc(upc).orElseThrow());
        return "storeproducts/receive";
    }

    @PostMapping("/{upc}/receive")
    @PreAuthorize("hasRole('MANAGER')")
    public String receive(@PathVariable String upc,
                          @RequestParam BigDecimal newPrice,
                          @RequestParam int addedQty,
                          RedirectAttributes ra) {
        try {
            service.receiveBatch(upc, newPrice, addedQty);
            ra.addFlashAttribute("success", "Партію прийнято, ціни переоцінено.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/store-products";
    }

    @PostMapping("/{upc}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String delete(@PathVariable String upc, RedirectAttributes ra) {
        try {
            service.delete(upc);
            ra.addFlashAttribute("success", "Товар у магазині видалено.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/store-products";
    }
}

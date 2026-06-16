package ua.zlagoda.service;

import org.springframework.stereotype.Service;
import ua.zlagoda.dao.CategoryDao;
import ua.zlagoda.model.Category;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryDao dao;

    public CategoryService(CategoryDao dao) {
        this.dao = dao;
    }

    public List<Category> findAll() { return dao.findAllOrderByName(); }

    public Optional<Category> findById(int id) { return dao.findById(id); }

    public Category create(Category c) {
        c.setCategoryNumber(dao.nextNumber());
        dao.insert(c);
        return c;
    }

    public void update(Category c) { dao.update(c); }

    public void delete(int id) {
        if (dao.countProducts(id) > 0) {
            throw new IllegalStateException("Неможливо видалити категорію: до неї належать товари.");
        }
        dao.delete(id);
    }
}

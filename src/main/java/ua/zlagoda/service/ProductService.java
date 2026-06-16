package ua.zlagoda.service;

import org.springframework.stereotype.Service;
import ua.zlagoda.dao.ProductDao;
import ua.zlagoda.model.Product;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductDao dao;

    public ProductService(ProductDao dao) {
        this.dao = dao;
    }

    public List<Product> findAll() { return dao.findAllOrderByName(); }

    public List<Product> findByCategory(int categoryNumber) { return dao.findByCategoryOrderByName(categoryNumber); }

    public List<Product> searchByName(String name) { return dao.searchByName(name); }

    public Optional<Product> findById(int id) { return dao.findById(id); }

    public Product create(Product p) {
        p.setIdProduct(dao.nextId());
        dao.insert(p);
        return p;
    }

    public void update(Product p) { dao.update(p); }

    public void delete(int id) {
        if (dao.countStoreProducts(id) > 0) {
            throw new IllegalStateException("Неможливо видалити товар: він присутній у магазині (товари у магазині).");
        }
        dao.delete(id);
    }
}

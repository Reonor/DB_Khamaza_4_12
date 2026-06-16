package ua.zlagoda.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.zlagoda.dao.StoreProductDao;
import ua.zlagoda.model.StoreProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Rules for «Товар у магазині».
 * <ul>
 *   <li>Promotional price = base price × 0.8 (standard 20% discount).</li>
 *   <li>Receiving a new batch re-prices the whole stock of that UPC (переоцінка) and
 *       adds the received quantity.</li>
 * </ul>
 */
@Service
public class StoreProductService {

    /** Standard promotional multiplier (−20%). */
    private static final BigDecimal PROMO_FACTOR = new BigDecimal("0.8");

    private final StoreProductDao dao;

    public StoreProductService(StoreProductDao dao) {
        this.dao = dao;
    }

    public List<StoreProduct> findAll(String promo, String sort) { return dao.findAll(promo, sort); }

    public List<StoreProduct> searchByProductName(String name) { return dao.searchByProductName(name); }

    public Optional<StoreProduct> findByUpc(String upc) { return dao.findByUpc(upc); }

    /** Regular (non-promotional) rows — used as the base when creating a promo item. */
    public List<StoreProduct> findRegular() { return dao.findRegular(); }

    @Transactional
    public StoreProduct create(StoreProduct sp) {
        sp.setUpc(dao.nextUpc());
        applyPromoPricing(sp);
        dao.insert(sp);
        return sp;
    }

    @Transactional
    public void update(StoreProduct sp) {
        applyPromoPricing(sp);
        dao.update(sp);
    }

    /** Переоцінка: new batch sets a new price for the whole stock and increases quantity. */
    @Transactional
    public void receiveBatch(String upc, BigDecimal newPrice, int addedQty) {
        if (newPrice == null || newPrice.signum() < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною.");
        }
        if (addedQty <= 0) {
            throw new IllegalArgumentException("Кількість нової партії повинна бути додатною.");
        }
        dao.receiveBatch(upc, newPrice, addedQty);
    }

    public void delete(String upc) {
        if (dao.countSales(upc) > 0) {
            throw new IllegalStateException("Неможливо видалити: товар присутній у чеках (історія збуту).");
        }
        dao.delete(upc);
    }

    /**
     * If the row is marked promotional and references a base regular item, derive its
     * price as base × 0.8. Otherwise leave the manually entered price untouched.
     */
    private void applyPromoPricing(StoreProduct sp) {
        if (sp.isPromotionalProduct() && sp.getUpcProm() != null && !sp.getUpcProm().isBlank()) {
            StoreProduct base = dao.findByUpc(sp.getUpcProm())
                    .orElseThrow(() -> new IllegalArgumentException("Базовий товар для акції не знайдено."));
            sp.setSellingPrice(base.getSellingPrice()
                    .multiply(PROMO_FACTOR)
                    .setScale(4, RoundingMode.HALF_UP));
        } else {
            sp.setPromotionalProduct(false);
            sp.setUpcProm(null);
            if (sp.getSellingPrice() == null) {
                throw new IllegalArgumentException("Вкажіть ціну продажу для звичайного товару.");
            }
        }
    }
}

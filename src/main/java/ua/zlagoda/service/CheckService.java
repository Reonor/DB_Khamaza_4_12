package ua.zlagoda.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.zlagoda.dao.CheckDao;
import ua.zlagoda.dao.CustomerCardDao;
import ua.zlagoda.dao.StoreProductDao;
import ua.zlagoda.model.Sale;
import ua.zlagoda.model.SaleCheck;
import ua.zlagoda.model.StoreProduct;
import ua.zlagoda.model.dto.Cart;
import ua.zlagoda.model.dto.CartLine;
import ua.zlagoda.model.dto.CheckView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Sale processing and check reporting.
 * <p>VAT follows the requirement literally: {@code vat = sum_total * 0.2}, i.e. the VAT
 * is the portion of the (already discounted) total to be paid.</p>
 */
@Service
public class CheckService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.2");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final CheckDao checkDao;
    private final StoreProductDao storeProductDao;
    private final CustomerCardDao customerCardDao;

    public CheckService(CheckDao checkDao, StoreProductDao storeProductDao, CustomerCardDao customerCardDao) {
        this.checkDao = checkDao;
        this.storeProductDao = storeProductDao;
        this.customerCardDao = customerCardDao;
    }

    /* ------------------------------------------------------------------ */
    /*  Cart building (cashier terminal)                                   */
    /* ------------------------------------------------------------------ */

    /** Adds a product (by UPC) to the working cart, checking that enough stock exists. */
    public void addToCart(Cart cart, String upc, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Кількість повинна бути додатною.");
        }
        StoreProduct sp = storeProductDao.findByUpc(upc)
                .orElseThrow(() -> new IllegalArgumentException("Товар з UPC " + upc + " не знайдено."));

        int already = cart.getLines().stream()
                .filter(l -> l.getUpc().equals(upc))
                .mapToInt(CartLine::getQuantity).sum();
        if (already + quantity > sp.getProductsNumber()) {
            throw new IllegalArgumentException("Недостатньо одиниць товару «" + sp.getProductName() +
                    "» на складі (доступно: " + sp.getProductsNumber() + ").");
        }
        cart.addOrIncrease(upc, sp.getProductName(), sp.getSellingPrice(), quantity);
    }

    /**
     * Finalises the cart into a stored check: writes the receipt and its sale lines,
     * decrements stock, and clears the cart. Runs in a single transaction.
     */
    @Transactional
    public String checkout(Cart cart, String idEmployee) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Чек порожній — додайте хоча б один товар.");
        }

        // Re-validate stock at the moment of sale.
        for (CartLine line : cart.getLines()) {
            StoreProduct sp = storeProductDao.findByUpc(line.getUpc())
                    .orElseThrow(() -> new IllegalStateException("Товар " + line.getUpc() + " більше не існує."));
            if (line.getQuantity() > sp.getProductsNumber()) {
                throw new IllegalStateException("Недостатньо одиниць товару «" + sp.getProductName() + "».");
            }
        }

        BigDecimal subtotal = cart.getSubtotal().setScale(4, RoundingMode.HALF_UP);
        BigDecimal sumTotal = applyDiscount(subtotal, cart.getCardNumber());
        BigDecimal vat = sumTotal.multiply(VAT_RATE).setScale(4, RoundingMode.HALF_UP);

        SaleCheck check = new SaleCheck();
        check.setCheckNumber(checkDao.nextCheckNumber());
        check.setIdEmployee(idEmployee);
        check.setCardNumber(cart.getCardNumber());
        check.setPrintDate(java.time.LocalDateTime.now());
        check.setSumTotal(sumTotal);
        check.setVat(vat);
        checkDao.insertReceipt(check);

        for (CartLine line : cart.getLines()) {
            Sale sale = new Sale();
            sale.setUpc(line.getUpc());
            sale.setCheckNumber(check.getCheckNumber());
            sale.setProductNumber(line.getQuantity());
            sale.setSellingPrice(line.getUnitPrice());
            checkDao.insertSale(sale);
            storeProductDao.decreaseStock(line.getUpc(), line.getQuantity());
        }

        String number = check.getCheckNumber();
        cart.clear();
        return number;
    }

    private BigDecimal applyDiscount(BigDecimal subtotal, String cardNumber) {
        if (cardNumber == null) {
            return subtotal;
        }
        return customerCardDao.findById(cardNumber)
                .map(card -> {
                    BigDecimal factor = HUNDRED.subtract(BigDecimal.valueOf(card.getDiscountPercent()))
                            .divide(HUNDRED, 6, RoundingMode.HALF_UP);
                    return subtotal.multiply(factor).setScale(4, RoundingMode.HALF_UP);
                })
                .orElse(subtotal);
    }

    /* ------------------------------------------------------------------ */
    /*  Reads / reports                                                    */
    /* ------------------------------------------------------------------ */

    public Optional<CheckView> view(String checkNumber) { return checkDao.findView(checkNumber); }

    public List<SaleCheck> byEmployeeAndPeriod(String idEmployee, LocalDate from, LocalDate to) {
        return checkDao.findByEmployeeAndPeriod(idEmployee, from, to);
    }

    public List<SaleCheck> byPeriod(LocalDate from, LocalDate to) {
        return checkDao.findByPeriod(from, to);
    }

    public List<SaleCheck> byEmployeeOnDay(String idEmployee, LocalDate day) {
        return checkDao.findByEmployeeOnDay(idEmployee, day);
    }

    public BigDecimal totalSumByEmployee(String idEmployee, LocalDate from, LocalDate to) {
        return checkDao.totalSumByEmployee(idEmployee, from, to);
    }

    public BigDecimal totalSumAll(LocalDate from, LocalDate to) {
        return checkDao.totalSumAll(from, to);
    }

    public long totalQuantityOfProduct(String upc, LocalDate from, LocalDate to) {
        return checkDao.totalQuantityOfProduct(upc, from, to);
    }

    @Transactional
    public void delete(String checkNumber) { checkDao.delete(checkNumber); }
}

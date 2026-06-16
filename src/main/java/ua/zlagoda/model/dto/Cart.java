package ua.zlagoda.model.dto;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Per-session working cart used by the cashier's sale terminal.
 * Holds line items and an optional customer card until the check is finalised.
 */
@Component
@SessionScope
public class Cart implements Serializable {

    private final List<CartLine> lines = new ArrayList<>();
    private String cardNumber;

    public List<CartLine> getLines() { return lines; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = (cardNumber == null || cardNumber.isBlank()) ? null : cardNumber;
    }

    /** Adds quantity to an existing line (same UPC) or creates a new one. */
    public void addOrIncrease(String upc, String productName, BigDecimal unitPrice, int quantity) {
        for (CartLine line : lines) {
            if (line.getUpc().equals(upc)) {
                line.setQuantity(line.getQuantity() + quantity);
                line.setUnitPrice(unitPrice); // keep current price
                return;
            }
        }
        lines.add(new CartLine(upc, productName, unitPrice, quantity));
    }

    public void remove(String upc) {
        lines.removeIf(l -> l.getUpc().equals(upc));
    }

    public void clear() {
        lines.clear();
        cardNumber = null;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public BigDecimal getSubtotal() {
        return lines.stream()
                .map(CartLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

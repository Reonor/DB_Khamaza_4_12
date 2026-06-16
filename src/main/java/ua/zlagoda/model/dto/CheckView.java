package ua.zlagoda.model.dto;

import ua.zlagoda.model.Sale;
import ua.zlagoda.model.SaleCheck;

import java.util.List;

/** A receipt together with all of its product lines, for viewing/printing. */
public class CheckView {

    private final SaleCheck check;
    private final List<Sale> lines;

    public CheckView(SaleCheck check, List<Sale> lines) {
        this.check = check;
        this.lines = lines;
    }

    public SaleCheck getCheck() { return check; }
    public List<Sale> getLines() { return lines; }
}

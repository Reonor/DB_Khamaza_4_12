package ua.zlagoda.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Renders a friendly error page for the most common application exceptions instead
 * of the default white-label error. Write-side handlers (create/update/delete) catch
 * these locally and redirect with a flash message; this advice covers anything else.
 */
@ControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public String accessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("message", "Недостатньо прав для виконання цієї дії.");
        return "error";
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String businessError(RuntimeException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}

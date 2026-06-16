package ua.zlagoda.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.zlagoda.model.Employee;

import java.util.Collection;
import java.util.List;

/**
 * Authenticated principal. Maps the business role («менеджер»/«касир») to a
 * Spring Security authority (ROLE_MANAGER / ROLE_CASHIER) and keeps the
 * employee id so controllers can scope data to "the current cashier".
 */
public class SecurityUser implements UserDetails {

    private final String idEmployee;
    private final String login;
    private final String passwordHash;
    private final String fullName;
    private final List<GrantedAuthority> authorities;

    public SecurityUser(Employee e) {
        this.idEmployee = e.getIdEmployee();
        this.login = e.getLogin();
        this.passwordHash = e.getPasswordHash();
        this.fullName = e.getFullName();
        String role = "менеджер".equals(e.getEmplRole()) ? "ROLE_MANAGER" : "ROLE_CASHIER";
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    public String getIdEmployee() { return idEmployee; }
    public String getFullName() { return fullName; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return login; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

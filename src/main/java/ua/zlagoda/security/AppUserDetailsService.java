package ua.zlagoda.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.zlagoda.dao.EmployeeDao;
import ua.zlagoda.model.Employee;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final EmployeeDao employeeDao;

    public AppUserDetailsService(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee e = employeeDao.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Користувача з логіном '" + username + "' не знайдено"));
        return new SecurityUser(e);
    }
}

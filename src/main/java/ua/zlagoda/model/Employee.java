package ua.zlagoda.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * «Працівник». Two business roles are stored in {@code emplRole}: «менеджер» / «касир».
 * {@code login} and {@code passwordHash} support authentication (passwords are never
 * stored in clear text – only the BCrypt hash is persisted).
 */
public class Employee {

    @NotBlank
    @Size(max = 10)
    private String idEmployee;

    @NotBlank
    @Size(max = 50)
    private String emplSurname;

    @NotBlank
    @Size(max = 50)
    private String emplName;

    @Size(max = 50)
    private String emplPatronymic;

    @NotBlank
    @Pattern(regexp = "менеджер|касир", message = "Посада має бути «менеджер» або «касир»")
    private String emplRole;

    @NotNull
    @DecimalMin(value = "0.0", message = "Зарплата не може бути від'ємною")
    private BigDecimal salary;

    @NotNull
    @Past(message = "Дата народження має бути в минулому")
    private LocalDate dateOfBirth;

    @NotNull
    private LocalDate dateOfStart;

    @NotBlank
    @Size(max = 13, message = "Номер телефону не може перевищувати 13 символів")
    private String phoneNumber;

    @NotBlank @Size(max = 50)
    private String city;

    @NotBlank @Size(max = 50)
    private String street;

    @NotBlank @Size(max = 9)
    private String zipCode;

    @NotBlank
    @Size(max = 50)
    private String login;

    /** BCrypt hash kept in DB. */
    private String passwordHash;

    /** Raw password coming from the form; hashed in the service, never persisted as-is. */
    private String password;

    public String getIdEmployee() { return idEmployee; }
    public void setIdEmployee(String idEmployee) { this.idEmployee = idEmployee; }

    public String getEmplSurname() { return emplSurname; }
    public void setEmplSurname(String emplSurname) { this.emplSurname = emplSurname; }

    public String getEmplName() { return emplName; }
    public void setEmplName(String emplName) { this.emplName = emplName; }

    public String getEmplPatronymic() { return emplPatronymic; }
    public void setEmplPatronymic(String emplPatronymic) { this.emplPatronymic = emplPatronymic; }

    public String getEmplRole() { return emplRole; }
    public void setEmplRole(String emplRole) { this.emplRole = emplRole; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDate getDateOfStart() { return dateOfStart; }
    public void setDateOfStart(LocalDate dateOfStart) { this.dateOfStart = dateOfStart; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(emplSurname).append(' ').append(emplName);
        if (emplPatronymic != null && !emplPatronymic.isBlank()) {
            sb.append(' ').append(emplPatronymic);
        }
        return sb.toString();
    }
}

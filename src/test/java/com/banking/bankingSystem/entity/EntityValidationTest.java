package com.banking.bankingSystem.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EntityValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    // ==========================================
    // ACCOUNT ENTITY VALIDATION TESTS
    // ==========================================

    @Test
    void shouldRejectBlankAccountNumber() {

        Account account = new Account();
        account.setAccountNumber(" ");

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "accountNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectAccountNumberExceedingMaximumLength() {

        Account account = new Account();
        account.setAccountNumber("A".repeat(21));

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "accountNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectNullCustomer() {

        Account account = new Account();
        account.setCustomer(null);

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "customer");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectNullAccountType() {

        Account account = new Account();
        account.setAccountType(null);

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "accountType");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectNullAccountStatus() {

        Account account = new Account();
        account.setStatus(null);

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "status");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectNegativeAccountBalance() throws Exception {

        Account account = new Account();

        setFieldValue(account, "balance", new BigDecimal("-100.00"));

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "balance");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectBalanceWithMoreThanTwoDecimalPlaces() throws Exception {

        Account account = new Account();

        setFieldValue(account, "balance", new BigDecimal("100.123"));

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "balance");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectBalanceExceedingMaximumPrecision() throws Exception {

        Account account = new Account();

        setFieldValue(
                account,
                "balance",
                new BigDecimal("100000000000000000.00")
        );

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "balance");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldAcceptValidAccountBalance() {

        Account account = new Account();
        account.setBalance(new BigDecimal("5000.00"));

        Set<ConstraintViolation<Account>> violations =
                validator.validateProperty(account, "balance");

        assertThat(violations).isEmpty();
    }

    // ==========================================
    // CUSTOMER ENTITY VALIDATION TESTS
    // ==========================================

    @Test
    void shouldRejectBlankCustomerName() {

        Customer customer = new Customer();
        customer.setFullName(" ");

        Set<ConstraintViolation<Customer>> violations =
                validator.validateProperty(customer, "fullName");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectCustomerNameExceedingMaximumLength() {

        Customer customer = new Customer();
        customer.setFullName("A".repeat(101));

        Set<ConstraintViolation<Customer>> violations =
                validator.validateProperty(customer, "fullName");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectBlankPhoneNumber() {

        Customer customer = new Customer();
        customer.setPhoneNumber(" ");

        Set<ConstraintViolation<Customer>> violations =
                validator.validateProperty(customer, "phoneNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectPhoneNumberExceedingMaximumLength() {

        Customer customer = new Customer();
        customer.setPhoneNumber("1234567890123456");

        Set<ConstraintViolation<Customer>> violations =
                validator.validateProperty(customer, "phoneNumber");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldRejectNullCustomerUser() {

        Customer customer = new Customer();
        customer.setUser(null);

        Set<ConstraintViolation<Customer>> violations =
                validator.validateProperty(customer, "user");

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldAcceptValidCustomerName() {

        Customer customer = new Customer();
        customer.setFullName("Rahul Sharma");

        Set<ConstraintViolation<Customer>> violations =
                validator.validateProperty(customer, "fullName");

        assertThat(violations).isEmpty();
    }

    // ==========================================
    // REFLECTION HELPER
    // ==========================================

    private void setFieldValue(
            Object object,
            String fieldName,
            Object value
    ) throws Exception {

        Field field = object.getClass().getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(object, value);
    }
}
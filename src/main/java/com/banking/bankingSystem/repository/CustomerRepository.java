package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser_UserId(Long userId);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

}
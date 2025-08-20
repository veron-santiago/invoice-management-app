package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByCompanyId(Long companyId);
    boolean existsByNameAndCompanyId(String name, Long companyId);
    Customer findByNameAndCompanyId(String name, Long companyId);
    Optional<Customer> findByCompanyIdAndNameIgnoreCase(Long companyId, String customerName);
}
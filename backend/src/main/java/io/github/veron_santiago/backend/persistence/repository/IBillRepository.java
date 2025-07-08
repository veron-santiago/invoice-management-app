package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IBillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByCompanyId(Long companyId);
    List<Bill> findByCustomerId(Long customerId);
}

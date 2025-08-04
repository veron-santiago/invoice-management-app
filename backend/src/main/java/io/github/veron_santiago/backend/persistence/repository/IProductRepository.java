package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCompanyId(Long companyId);
    Product findByNameAndCompanyId(String name, Long companyId);
    Optional<Product> findByCodeAndCompanyId(String code, Long companyId);
    boolean existsByNameAndCompanyId(String name, Long companyId);
    boolean existsByCodeAndCompanyId(String code, Long companyId);
    boolean existsByNameAndCompanyIdAndIdNot(String name, Long companyId, Long id);
    boolean existsByCodeAndCompanyIdAndIdNot(String code, Long companyId, Long id);
}
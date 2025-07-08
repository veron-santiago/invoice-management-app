package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCompanyId(Long companyId);
}
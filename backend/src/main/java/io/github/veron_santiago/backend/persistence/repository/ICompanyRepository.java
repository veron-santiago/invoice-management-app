package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyName(String companyName);
    Optional<Company> findByEmail(String email);
    @Query("SELECT c FROM Company c WHERE c.companyName = :value OR c.email = :value")
    Optional<Company> findByCompanyNameOrEmail(@Param("value") String companyNameOrEmail);
    @Query("SELECT c.email FROM Company c WHERE c.companyName = :companyName")
    Optional<String> findEmailByCompanyName(@Param("companyName") String companyName);
    boolean existsByCompanyName(String companyName);
    boolean existsByEmail(String email);
    @Query("SELECT c.id FROM Company c WHERE c.companyName = :companyName")
    Optional<Long> findIdByCompanyName(@Param("companyName") String companyName);
    @Query("SELECT c.id FROM Company c WHERE c.email = :email")
    Optional<Long> findIdByEmail(@Param("email") String email);}

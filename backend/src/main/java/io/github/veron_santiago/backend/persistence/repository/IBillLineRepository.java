package io.github.veron_santiago.backend.persistence.repository;

import io.github.veron_santiago.backend.persistence.entity.BillLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBillLineRepository extends JpaRepository<BillLine, Long> {
}

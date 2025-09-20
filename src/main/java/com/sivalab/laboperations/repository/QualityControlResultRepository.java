package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.QualityControlResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityControlResultRepository extends JpaRepository<QualityControlResult, Long> {
}

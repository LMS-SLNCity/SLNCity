package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.QualityIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityIndicatorRepository extends JpaRepository<QualityIndicator, Long> {
}

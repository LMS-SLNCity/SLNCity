package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.QualityControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityControlRepository extends JpaRepository<QualityControl, Long> {
}

package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.ProficiencyTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProficiencyTestRepository extends JpaRepository<ProficiencyTest, Long> {
}

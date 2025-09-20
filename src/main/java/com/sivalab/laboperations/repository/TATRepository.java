package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.TAT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TATRepository extends JpaRepository<TAT, Long> {
    Optional<TAT> findByTestTemplate_TemplateId(Long testTemplateId);
}

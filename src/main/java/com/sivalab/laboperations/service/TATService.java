package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.TAT;

import java.util.Optional;

public interface TATService {
    TAT createTAT(Long testTemplateId, int tatValue, TAT.TATUnit tatUnit);
    Optional<TAT> getTATByTestTemplate_TemplateId(Long testTemplateId);
    TAT updateTAT(Long id, int tatValue, TAT.TATUnit tatUnit);
    void deleteTAT(Long id);
}

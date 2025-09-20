package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.QualityControlResult;
import java.util.List;

public interface WestgardService {
    boolean evaluate(List<QualityControlResult> results, String rules);
}

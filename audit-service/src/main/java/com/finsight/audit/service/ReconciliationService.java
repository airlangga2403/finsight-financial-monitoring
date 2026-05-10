package com.finsight.audit.service;

import com.finsight.audit.dto.response.MonthlySummaryResponse;
import com.finsight.audit.dto.response.ReconciliationDetailResponse;
import com.finsight.audit.dto.response.ReconciliationResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReconciliationService {

    ReconciliationResponse runReconciliation(LocalDate date);

    ReconciliationDetailResponse getReconciliationDetail(String recordId);

    ReconciliationDetailResponse getReconciliationByDate(LocalDate date);

    List<ReconciliationResponse> getHistory();

    List<ReconciliationResponse> getDiscrepancyHistory(LocalDate fromDate, LocalDate toDate, double minDiscrepancy);

    List<MonthlySummaryResponse> getMonthlySummary(int year);
}

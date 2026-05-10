package com.finsight.transaction.service;

import com.finsight.transaction.dto.request.CreateTransactionRequest;
import com.finsight.transaction.dto.request.TransactionFilterRequest;
import com.finsight.transaction.dto.response.DailyVolumeResponse;
import com.finsight.transaction.dto.response.DashboardSummaryResponse;
import com.finsight.transaction.dto.response.SuspiciousTransactionResponse;
import com.finsight.transaction.dto.response.TransactionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    TransactionResponse processTransaction(CreateTransactionRequest request);

    TransactionResponse findById(String id);

    TransactionResponse findByReferenceNumber(String referenceNumber);

    List<TransactionResponse> findByAccountId(String accountId);

    List<SuspiciousTransactionResponse> findSuspiciousTransactions(TransactionFilterRequest filter);

    List<DailyVolumeResponse> getDailyVolumeReport(LocalDateTime startDate, LocalDateTime endDate);

    DashboardSummaryResponse getDashboardSummary();
}

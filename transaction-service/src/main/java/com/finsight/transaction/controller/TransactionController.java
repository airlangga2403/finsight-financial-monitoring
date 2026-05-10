package com.finsight.transaction.controller;

import com.finsight.transaction.dto.request.CreateTransactionRequest;
import com.finsight.transaction.dto.request.TransactionFilterRequest;
import com.finsight.transaction.dto.response.ApiResponse;
import com.finsight.transaction.dto.response.SuspiciousTransactionResponse;
import com.finsight.transaction.dto.response.TransactionResponse;
import com.finsight.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Processing", description = "Submit and query financial transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Submit a new transaction (CREDIT / DEBIT / TRANSFER)")
    public ResponseEntity<ApiResponse<TransactionResponse>> processTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.processTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transaction processed", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.findById(id)));
    }

    @GetMapping("/ref/{referenceNumber}")
    @Operation(summary = "Get transaction by reference number")
    public ResponseEntity<ApiResponse<TransactionResponse>> findByRef(
            @PathVariable String referenceNumber) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.findByReferenceNumber(referenceNumber)));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get all transactions for an account")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> findByAccount(
            @PathVariable String accountId) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.findByAccountId(accountId)));
    }

    @GetMapping("/suspicious")
    @Operation(summary = "Query suspicious/flagged transactions",
               description = "All parameters are optional — can be combined for fine-grained filtering")
    public ResponseEntity<ApiResponse<List<SuspiciousTransactionResponse>>> findSuspicious(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) BigDecimal minAmount) {

        TransactionFilterRequest filter = new TransactionFilterRequest();
        filter.setAccountId(accountId);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setMinAmount(minAmount);

        return ResponseEntity.ok(ApiResponse.ok(transactionService.findSuspiciousTransactions(filter)));
    }
}

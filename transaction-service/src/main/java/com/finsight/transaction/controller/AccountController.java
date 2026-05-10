package com.finsight.transaction.controller;

import com.finsight.transaction.dto.request.CreateAccountRequest;
import com.finsight.transaction.dto.response.AccountNetPositionResponse;
import com.finsight.transaction.dto.response.AccountResponse;
import com.finsight.transaction.dto.response.ApiResponse;
import com.finsight.transaction.enums.AccountStatus;
import com.finsight.transaction.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Create and manage bank accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<ApiResponse<AccountResponse>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.findById(id)));
    }

    @GetMapping("/by-number/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<ApiResponse<AccountResponse>> findByAccountNumber(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.findByAccountNumber(accountNumber)));
    }

    @GetMapping
    @Operation(summary = "List all accounts", description = "Optionally filter by status")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> findAll(
            @RequestParam(required = false) AccountStatus status) {
        List<AccountResponse> accounts = status != null
                ? accountService.findByStatus(status)
                : accountService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(accounts));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update account status (ACTIVE / SUSPENDED / CLOSED)")
    public ResponseEntity<ApiResponse<AccountResponse>> updateStatus(
            @PathVariable String id,
            @RequestParam AccountStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Account status updated", accountService.updateStatus(id, status)));
    }

    @GetMapping("/net-positions")
    @Operation(summary = "Get net credit/debit position per active account",
               description = "Used by the audit-service for daily reconciliation")
    public ResponseEntity<ApiResponse<List<AccountNetPositionResponse>>> getNetPositions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccountNetPositions(startDate, endDate)));
    }
}

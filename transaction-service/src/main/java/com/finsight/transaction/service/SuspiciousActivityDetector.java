package com.finsight.transaction.service;

import com.finsight.transaction.entity.Account;
import com.finsight.transaction.entity.Transaction;

import java.util.Optional;

public interface SuspiciousActivityDetector {

    Optional<String> detect(Transaction transaction, Account sourceAccount);
}

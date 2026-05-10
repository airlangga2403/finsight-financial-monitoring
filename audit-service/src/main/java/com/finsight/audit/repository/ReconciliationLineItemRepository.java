package com.finsight.audit.repository;

import com.finsight.audit.entity.ReconciliationLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationLineItemRepository extends JpaRepository<ReconciliationLineItem, String> {

    List<ReconciliationLineItem> findByReconciliationRecordId(String reconciliationRecordId);

}

package com.finsight.audit.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ReconciliationDetailResponse {
    private ReconciliationResponse summary;
    private List<LineItemResponse> lineItems;
    private List<LineItemResponse> discrepancies;
    private long discrepancyCount;
}

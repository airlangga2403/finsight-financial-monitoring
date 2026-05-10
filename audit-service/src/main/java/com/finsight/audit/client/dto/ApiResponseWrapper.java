package com.finsight.audit.client.dto;

import lombok.Data;

@Data
public class ApiResponseWrapper<T> {
    private boolean success;
    private String  message;
    private T       data;
    private String  errorCode;
}


package com.example.dto;

import java.time.LocalDateTime;

public class StatusHistoryDTO {
    private String status;
    private LocalDateTime changedAt;
    private String operator;

    // 构造方法
    public StatusHistoryDTO(String status, LocalDateTime changedAt, String operator) {
        this.status = status;
        this.changedAt = changedAt;
        this.operator = operator;
    }

    // Getter方法
    public String getStatus() {
        return status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getOperator() {
        return operator;
    }
}
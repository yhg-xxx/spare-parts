package com.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class InboundBatchCreateRequest {
    @NotNull(message = "采购单号不能为空")
    private Integer orderId;

    @Valid
    @NotEmpty(message = "入库明细不能为空")
    private List<InboundCreateRequest> requests;

    // 必须添加无参构造
    public InboundBatchCreateRequest() {}

    // Getter/Setter
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public List<InboundCreateRequest> getRequests() { return requests; }
    public void setRequests(List<InboundCreateRequest> requests) { this.requests = requests; }
}
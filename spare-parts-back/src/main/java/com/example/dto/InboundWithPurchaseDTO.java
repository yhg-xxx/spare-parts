package com.example.dto;

import com.example.entity.InboundRecord;
import com.example.entity.Purchase_order;

public class InboundWithPurchaseDTO {
    private InboundRecord inboundRecord;
    private Purchase_order purchaseOrder;

    // 构造函数
    public InboundWithPurchaseDTO(InboundRecord inboundRecord, Purchase_order purchase_order) {
        this.inboundRecord = inboundRecord;
        this.purchaseOrder = purchase_order;
    }

    // Getter/Setter
    public InboundRecord getInboundRecord() {
        return inboundRecord;
    }

    public void setInboundRecord(InboundRecord inboundRecord) {
        this.inboundRecord = inboundRecord;
    }

    public Purchase_order getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(Purchase_order purchase_order) {
        this.purchaseOrder = purchase_order;
    }
}
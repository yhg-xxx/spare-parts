package com.example.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_order_status_history")
public class PurchaseOrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Purchase_order purchaseOrder;

    @Column(name = "status", nullable = false, length = 255)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false, columnDefinition = "datetime")
    private LocalDateTime changedAt;

    // 构造方法
    public PurchaseOrderStatusHistory() {
    }

    public PurchaseOrderStatusHistory(Purchase_order purchaseOrder, String status, User changedBy, LocalDateTime changedAt) {
        this.purchaseOrder = purchaseOrder;
        this.status = status;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    // Getter/Setter
    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public Purchase_order getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(Purchase_order purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    // toString()（可选）
    @Override
    public String toString() {
        return "PurchaseOrderStatusHistory{" +
                "historyId=" + historyId +
                ", status='" + status + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
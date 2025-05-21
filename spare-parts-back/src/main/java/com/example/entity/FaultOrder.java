package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "fault_order",
        indexes = {
                @Index(name = "idx_part_id", columnList = "part_id"),
                @Index(name = "idx_status", columnList = "work_order_status"),
                @Index(name = "idx_created_at", columnList = "created_at")
        })

public class FaultOrder {

    public enum WorkOrderStatus {
        待处理, 处理中, 待验收, 已验收, 已返厂, 已报废, 已关闭
    }

    public enum ReviewResult {
        通过, 驳回
    }

    public enum DisposalType {
        返厂修, 报废, 修好件
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fault_id")
    private Long faultId;

    // 关联备件表（多对一）
    @ManyToOne
    @JoinColumn(name = "part_id", referencedColumnName = "part_id", nullable = false)
    private Spare_part sparePart;

    @Column(name = "sn", nullable = false, length = 255)
    private String sn;

    @Column(name = "fault_time",  nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime faultTime;

    @Column(name = "reported_by", length = 255)
    private String reportedBy;

    @Column(name = "fault_description", columnDefinition = "TEXT")
    private String faultDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_order_status", nullable = false, length = 20)
    private WorkOrderStatus workOrderStatus = WorkOrderStatus.待处理;

    @Column(name = "repair_result")
    private String repairResult;

    @Column(name = "repair_by", length = 255)
    private String repairBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_result", length = 10)
    private ReviewResult reviewResult;

    @Column(name = "review_by", length = 255)
    private String reviewBy;

    @Column(name = "review_at")
    private LocalDateTime reviewAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposal_type", length = 10)
    private DisposalType disposalType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public Long getFaultId() {
        return faultId;
    }

    public void setFaultId(Long faultId) {
        this.faultId = faultId;
    }

    public Spare_part getSparePart() {
        return sparePart;
    }

    public void setSparePart(Spare_part sparePart) {
        this.sparePart = sparePart;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public LocalDateTime getFaultTime() {
        return faultTime;
    }

    public void setFaultTime(LocalDateTime faultTime) {
        this.faultTime = faultTime;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    public WorkOrderStatus getWorkOrderStatus() {
        return workOrderStatus;
    }

    public void setWorkOrderStatus(WorkOrderStatus workOrderStatus) {
        this.workOrderStatus = workOrderStatus;
    }

    public String getRepairResult() {
        return repairResult;
    }

    public void setRepairResult(String repairResult) {
        this.repairResult = repairResult;
    }

    public String getRepairBy() {
        return repairBy;
    }

    public void setRepairBy(String repairBy) {
        this.repairBy = repairBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public ReviewResult getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(ReviewResult reviewResult) {
        this.reviewResult = reviewResult;
    }

    public String getReviewBy() {
        return reviewBy;
    }

    public void setReviewBy(String reviewBy) {
        this.reviewBy = reviewBy;
    }

    public LocalDateTime getReviewAt() {
        return reviewAt;
    }

    public void setReviewAt(LocalDateTime reviewAt) {
        this.reviewAt = reviewAt;
    }

    public DisposalType getDisposalType() {
        return disposalType;
    }

    public void setDisposalType(DisposalType disposalType) {
        this.disposalType = disposalType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
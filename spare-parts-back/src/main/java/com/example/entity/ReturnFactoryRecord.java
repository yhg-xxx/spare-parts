package com.example.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "return_factory_record")
public class ReturnFactoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Integer returnId;

    @Column(name = "fault_id", nullable = false)
    private Long faultId;

    @Column(name = "part_id", nullable = false)
    private Integer partId;

    @Column(name = "sn", nullable = false, length = 255)
    private String sn;

    @Column(name = "return_reason", nullable = false, columnDefinition = "TEXT")
    private String returnReason;

    @Column(name = "expected_repair_days", nullable = false)
    private Integer expectedRepairDays;

    @Column(name = "logistics_number", length = 100)
    private String logisticsNumber;

    @Column(name = "logistics_company", length = 100)
    private String logisticsCompany;

    @Column(name = "sent_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentTime;

    @Column(name = "expected_return_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expectedReturnTime;

    @Column(name = "actual_return_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualReturnTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "repair_result", columnDefinition = "ENUM('修复成功','修复失败','未修复')")
    private RepairResult repairResult;

    @Column(name = "repair_description", columnDefinition = "TEXT")
    private String repairDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('待返厂','已返厂','维修中','已返回','已验收','已报废')")
    private ReturnStatus status = ReturnStatus.待返厂;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // 枚举定义
    public enum RepairResult {
        修复成功, 修复失败, 未修复
    }

    public enum ReturnStatus {
        待返厂, 已返厂, 维修中, 已返回, 已验收, 已报废
    }

    // Getters and Setters
    public Integer getReturnId() {
        return returnId;
    }

    public void setReturnId(Integer returnId) {
        this.returnId = returnId;
    }

    public Long getFaultId() {
        return faultId;
    }

    public void setFaultId(Long faultId) {
        this.faultId = faultId;
    }

    public Integer getPartId() {
        return partId;
    }

    public void setPartId(Integer partId) {
        this.partId = partId;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public Integer getExpectedRepairDays() {
        return expectedRepairDays;
    }

    public void setExpectedRepairDays(Integer expectedRepairDays) {
        this.expectedRepairDays = expectedRepairDays;
    }

    public String getLogisticsNumber() {
        return logisticsNumber;
    }

    public void setLogisticsNumber(String logisticsNumber) {
        this.logisticsNumber = logisticsNumber;
    }

    public String getLogisticsCompany() {
        return logisticsCompany;
    }

    public void setLogisticsCompany(String logisticsCompany) {
        this.logisticsCompany = logisticsCompany;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public Date getExpectedReturnTime() {
        return expectedReturnTime;
    }

    public void setExpectedReturnTime(Date expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }

    public Date getActualReturnTime() {
        return actualReturnTime;
    }

    public void setActualReturnTime(Date actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }

    public RepairResult getRepairResult() {
        return repairResult;
    }

    public void setRepairResult(RepairResult repairResult) {
        this.repairResult = repairResult;
    }

    public String getRepairDescription() {
        return repairDescription;
    }

    public void setRepairDescription(String repairDescription) {
        this.repairDescription = repairDescription;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnStatus status) {
        this.status = status;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
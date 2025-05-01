package com.example.entity;

import jakarta.persistence.*;


import java.io.Serializable;

@Entity

@Table(name = "scrap_records")
public class ScrapRecord implements Serializable {

    // 核心标识
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ 添加自增策略
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "sn", nullable = false, length = 255, unique = true)
    private String sn;

    // 申请信息
    @Column(name = "scrap_reason", nullable = false, length = 255)
    private String scrapReason;

    @Column(name = "damage_photo", length = 255)
    private String damagePhoto;

    @Column(name = "apply_time", nullable = false, length = 255)
    private String applyTime;




    // 执行信息
    @Column(name = "scrap_time", length = 255)
    private String scrapTime;

    @Column(name = "executor", length = 255)
    private String executor;

    @Column(name = "disposal_method", length = 255)
    private String disposalMethod;

    // 状态联动（新增待审核状态）
    @Enumerated(EnumType.STRING)
    @Column(name = "part_status", columnDefinition = "ENUM('待审核','已报废','驳回')")
    private PartStatus partStatus;
    @Column(name = "applicant_id", nullable = false, length = 255)
    private String applicantId;

    public enum PartStatus {
        待审核, 已报废,驳回
    }

    // 无参构造
    public ScrapRecord() {
    }


    public ScrapRecord(Integer orderId, String sn, String scrapReason, String damagePhoto, String applyTime, String scrapTime, String executor, String disposalMethod, PartStatus partStatus, String applicantId) {
        this.orderId = orderId;
        this.sn = sn;
        this.scrapReason = scrapReason;
        this.damagePhoto = damagePhoto;
        this.applyTime = applyTime;

        this.scrapTime = scrapTime;
        this.executor = executor;
        this.disposalMethod = disposalMethod;
        this.partStatus = partStatus;
        this.applicantId = applicantId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getScrapReason() {
        return scrapReason;
    }

    public void setScrapReason(String scrapReason) {
        this.scrapReason = scrapReason;
    }

    public String getDamagePhoto() {
        return damagePhoto;
    }

    public void setDamagePhoto(String damagePhoto) {
        this.damagePhoto = damagePhoto;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }


    public String getScrapTime() {
        return scrapTime;
    }

    public void setScrapTime(String scrapTime) {
        this.scrapTime = scrapTime;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getDisposalMethod() {
        return disposalMethod;
    }

    public void setDisposalMethod(String disposalMethod) {
        this.disposalMethod = disposalMethod;
    }

    public PartStatus getPartStatus() {
        return partStatus;
    }

    public void setPartStatus(PartStatus partStatus) {
        this.partStatus = partStatus;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }
}
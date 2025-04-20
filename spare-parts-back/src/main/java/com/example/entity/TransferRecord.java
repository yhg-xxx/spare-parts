package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transfer_record")
public class TransferRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transferId;

    @Column(name = "from_location_name")
    private String fromLocationName;

    @Column(name = "to_location_name")
    private String toLocationName;

    @Column(name = "part_name")
    private String partName;

    @Column(name = "transfer_reason")
    private String transferReason;

    @Column(name = "applicant_id")
    private int applicantId;

    private String status;

    @Column(name = "sn")  // 新增数量字段
    private String  an;


    @Column(name = "created_at")
    private String createdAt;


    // 所有getter和setter方法
    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public String getFromLocationName() {
        return fromLocationName;
    }

    public void setFromLocationName(String fromLocationName) {
        this.fromLocationName = fromLocationName;
    }

    public String getToLocationName() {
        return toLocationName;
    }

    public void setToLocationName(String toLocationName) {
        this.toLocationName = toLocationName;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public int getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(int applicantId) {
        this.applicantId = applicantId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getAn() {
        return an;
    }

    public void setAn(String an) {
        this.an = an;
    }
}
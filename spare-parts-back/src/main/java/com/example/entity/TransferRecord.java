package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transfer_record")
public class TransferRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transferId;

    @Column(name = "from_location_id")
    private int fromLocationId;

    @Column(name = "to_location_id")
    private int toLocationId;

    @Column(name = "part_id")
    private int partId;

    @Column(name = "transfer_reason")
    private String transferReason;

    @Column(name = "applicant_id")
    private int applicantId;

    private String status;


    @Column(name = "created_at")
    private String createdAt;


    // 所有getter和setter方法
    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getFromLocationId() {
        return fromLocationId;
    }

    public void setFromLocationId(int fromLocationId) {
        this.fromLocationId = fromLocationId;
    }

    public int getToLocationId() {
        return toLocationId;
    }

    public void setToLocationId(int toLocationId) {
        this.toLocationId = toLocationId;
    }

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
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



}
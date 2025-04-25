package com.example.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "stockout")
public class Stockout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT COMMENT '出库id'")
    private Integer id;

    @Column(name = "request_id", columnDefinition = "INT COMMENT '关联领用单'")
    private Integer requestId;

    @Column(name = "part_name", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备件名称'")
    private String partName;

    @Column(name = "part_model", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备件型号'")
    private String partModel;

    @Column(name = "sn", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备件SN'")
    private String sn;

    @Column(name = "operator_id", columnDefinition = "INT COMMENT '出库操作员'")
    private Integer operatorId;
    @Column(name = "location_id", columnDefinition = "varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '仓库ID'")
    private String locationId;

    @Column(name = "location_name", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '出库仓库'")
    private String locationName;

    @Column(name = "out_time", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '出库时间'")
    private String outTime;

    @Column(name = "status", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '出库状态'")
    private String status;

    // Getters and Setters

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getPartModel() {
        return partModel;
    }

    public void setPartModel(String partModel) {
        this.partModel = partModel;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Stockout() {
    }

    public Stockout(Integer id, Integer requestId, String partName, String partModel, String sn, Integer operatorId, String locationId, String locationName, String outTime, String status) {
        this.id = id;
        this.requestId = requestId;
        this.partName = partName;
        this.partModel = partModel;
        this.sn = sn;
        this.operatorId = operatorId;
        this.locationId = locationId;
        this.locationName = locationName;
        this.outTime = outTime;
        this.status = status;
    }
}
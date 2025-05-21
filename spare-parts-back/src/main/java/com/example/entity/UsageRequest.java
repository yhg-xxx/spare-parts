package com.example.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "usagerequest", schema = "parts2",
        indexes = {
                @Index(name = "idx_location_id", columnList = "location_id") // 显式声明外键索引
        })
public class UsageRequest implements Serializable {

    public enum UsageType {
        维修申领, 维修借用
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "INT COMMENT '领用单号'")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "ddd-p"))
    private Warehouse warehouse; // 关联仓库位置

    @Column(name = "applicant_id", columnDefinition = "INT COMMENT '申请人'")
    private Integer applicantId;

    @Column(name = "part_name", columnDefinition = "VARCHAR(255) COMMENT '备件名称'")
    private String partName;

    @Column(name = "part_model", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备件型号'")
    private String partModel;

    @Column(name = "description", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '申请说明'")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "ENUM('维修申领','维修借用') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '类型'")
    private UsageType type;

    @Column(name = "status", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '状态'")
    private String status;

    @Column(name = "create_time", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '创建时间'")
    private String createTime;
    @Column(name = "number", columnDefinition = "INT DEFAULT NULL COMMENT '数量'")
    private Integer number;

    // Getter & Setter
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }

    // 无参构造函数
    public UsageRequest() {}

    // Getter & Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Integer getApplicantId() { return applicantId; }
    public void setApplicantId(Integer applicantId) { this.applicantId = applicantId; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getPartModel() { return partModel; }
    public void setPartModel(String partModel) { this.partModel = partModel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UsageType getType() { return type; }
    public void setType(UsageType type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    // toString()
    @Override
    public String toString() {
        return "UsageRequest{" +
                "id=" + id +
                ", warehouse=" + (warehouse != null ? warehouse.getLocation_id() : "null") +
                ", applicantId=" + applicantId +
                ", partName='" + partName + '\'' +
                ", partModel='" + partModel + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", status='" + status + '\'' +
                ", createTime='" + createTime + '\'' +
                ", number=" + number +
                '}';
    }
}
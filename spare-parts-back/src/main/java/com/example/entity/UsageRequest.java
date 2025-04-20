package com.example.entity;

import jakarta.persistence.*;


import java.io.Serializable;

@Entity
@Table(name = "usagerequest", schema = "your_schema_name") // 替换为你的数据库schema名
public class UsageRequest implements Serializable {

    public enum UsageType {
        维修申领, 维修借用
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "INT COMMENT '领用单号'")
    private Integer id;

    @Column(name = "applicant_id", columnDefinition = "INT COMMENT '申请人'")
    private Integer applicantId;



    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "ENUM('维修申领','维修借用') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '类型'")
    private UsageType type;

    @Column(name = "status", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '状态'")
    private String status;

    @Column(name = "create_time", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '创建时间'")
    private String createTime;

    // 无参构造函数（JPA 要求）
    public UsageRequest() {
    }

    // Getter & Setter
    // 这里可以使用 Lombok 的 @Data 注解简化
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Integer applicantId) {
        this.applicantId = applicantId;
    }


    public UsageType getType() {
        return type;
    }

    public void setType(UsageType type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    // toString() 方法
    @Override
    public String toString() {
        return "UsageRequest{" +
                "id=" + id +
                ", applicantId=" + applicantId +
                ", type=" + type +
                ", status='" + status + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
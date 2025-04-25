package com.example.entity;

import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "spare_part")

public class Spare_part {

    // 枚举定义
    public enum Category {
        机械类, 电气类, 液压类, 电子类, 其他
    }

    public enum SparePartStatus {
        新好件, 修好件, 坏件, 二级修, 返厂修, 待调拨, 已报废
    }

    public enum SparePartType {
        正常件, 在保件, 遗留件
    }
    // 关联故障工单（一对多）
    @OneToMany(mappedBy = "sparePart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FaultOrder> faultOrders;
    // 实体字段
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_id", nullable = false)
    private Integer partId;

    @Column(name = "part_name", nullable = false)
    private String partName;


    @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "qqq-o"))
    private Integer locationId;

    @Column(name = "part_model")
    private String partModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", columnDefinition = "ENUM('机械类','电气类','液压类','电子类','其他')")
    private Category category = Category.机械类;

    @Enumerated(EnumType.STRING)
    @Column(name = "spare_part_status", columnDefinition = "ENUM('新好件','修好件','坏件','二级修','返厂修','待调拨','已报废')")
    private SparePartStatus sparePartStatus = SparePartStatus.新好件;

    @Enumerated(EnumType.STRING)
    @Column(name = "spare_part_type", columnDefinition = "ENUM('正常件','在保件','遗留件')")
    private SparePartType sparePartType = SparePartType.正常件;

    @Column(name = "sn")
    private String sn;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "unit")
    private String unit;
    @Column(name = "status")
    private String status;
    public Integer getPartId() {
        return partId;
    }

    public void setPartId(Integer partId) {
        this.partId = partId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getPartModel() {
        return partModel;
    }

    public void setPartModel(String partModel) {
        this.partModel = partModel;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public SparePartStatus getSparePartStatus() {
        return sparePartStatus;
    }

    public void setSparePartStatus(SparePartStatus sparePartStatus) {
        this.sparePartStatus = sparePartStatus;
    }

    public SparePartType getSparePartType() {
        return sparePartType;
    }

    public void setSparePartType(SparePartType sparePartType) {
        this.sparePartType = sparePartType;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
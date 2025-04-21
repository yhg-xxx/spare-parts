package com.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inbound_record",
        indexes = {
                @Index(name = "order_id_index", columnList = "order_id"),
                @Index(name = "location_id_index", columnList = "location_id")
        })
public class InboundRecord {

    public enum SparePartCategory {
        机械类, 电气类, 液压类, 电子类, 其他
    }

    public enum SparePartStatus {
        新好件, 修好件, 坏件, 二级修, 返厂修, 待调拨, 待报废, 已报废
    }

    public enum SparePartType {
        正常件, 在保件, 遗留件
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_id")
    private Integer inboundId;
    // 新增关联字段
    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false) // 映射到数据库外键列 order_id
    private Purchase_order purchaseOrder;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "location_id", nullable = false)
    private Integer locationId;



    @Enumerated(EnumType.STRING)
    @Column(name = "spare_part_category", nullable = false, length = 20)
    private SparePartCategory sparePartCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "spare_part_status", nullable = false, length = 20)
    private SparePartStatus sparePartStatus = SparePartStatus.新好件;

    @Enumerated(EnumType.STRING)
    @Column(name = "spare_part_type", nullable = false, length = 20)
    private SparePartType sparePartType = SparePartType.正常件;



    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_free_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxFreeUnitPrice;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "tax_free_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxFreeTotal;

    @Column(name = "total_tax", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalTax;

    @Column(length = 100)
    private String sn;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "warranty_until")
    private LocalDate warrantyUntil;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    // 新增预处理方法
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    // 默认构造函数
    public InboundRecord() {
    }

    public Purchase_order getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(Purchase_order purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public Integer getInboundId() {
        return inboundId;
    }

    public void setInboundId(Integer inboundId) {
        this.inboundId = inboundId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }



    public SparePartCategory getSparePartCategory() {
        return sparePartCategory;
    }

    public void setSparePartCategory(SparePartCategory sparePartCategory) {
        this.sparePartCategory = sparePartCategory;
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


    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxFreeUnitPrice() {
        return taxFreeUnitPrice;
    }

    public void setTaxFreeUnitPrice(BigDecimal taxFreeUnitPrice) {
        this.taxFreeUnitPrice = taxFreeUnitPrice;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTaxFreeTotal() {
        return taxFreeTotal;
    }

    public void setTaxFreeTotal(BigDecimal taxFreeTotal) {
        this.taxFreeTotal = taxFreeTotal;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDate getWarrantyUntil() {
        return warrantyUntil;
    }

    public void setWarrantyUntil(LocalDate warrantyUntil) {
        this.warrantyUntil = warrantyUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
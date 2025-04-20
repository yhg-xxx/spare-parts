package com.example.dto;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InboundCreateRequest {
    @NotNull(message = "库位ID不能为空")
    private Integer locationId;
    // 新增字段
    @NotBlank(message = "SN码不能为空")
    private String sn;
    @NotBlank(message = "备件分类不能为空")
    private String sparePartCategory;

    @NotBlank(message = "备件状态不能为空")
    private String sparePartStatus;

    @NotBlank(message = "备件类型不能为空")
    private String sparePartType;

    @DecimalMin(value = "0.0", message = "单价必须大于0")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "税率必须大于0")
    @DecimalMax(value = "1.0", message = "税率不能超过100%")
    private BigDecimal taxRate;

    private String unit;
    private String manufacturer;

    @Future(message = "保修截止日期必须是将来时间")
    private LocalDate warrantyUntil;

    // Getter和Setter
    public Integer getLocationId() { return locationId; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }

    public @NotBlank(message = "备件分类不能为空") String getSparePartCategory() {
        return sparePartCategory;
    }

    public void setSparePartCategory(@NotBlank(message = "备件分类不能为空") String sparePartCategory) {
        this.sparePartCategory = sparePartCategory;
    }

    public @NotBlank(message = "备件状态不能为空") String getSparePartStatus() {
        return sparePartStatus;
    }

    public void setSparePartStatus(@NotBlank(message = "备件状态不能为空") String sparePartStatus) {
        this.sparePartStatus = sparePartStatus;
    }

    public @NotBlank(message = "备件类型不能为空") String getSparePartType() {
        return sparePartType;
    }

    public void setSparePartType(@NotBlank(message = "备件类型不能为空") String sparePartType) {
        this.sparePartType = sparePartType;
    }

    public @DecimalMin(value = "0.0", message = "单价必须大于0") BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(@DecimalMin(value = "0.0", message = "单价必须大于0") BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public @DecimalMin(value = "0.0", message = "税率必须大于0") @DecimalMax(value = "1.0", message = "税率不能超过100%") BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(@DecimalMin(value = "0.0", message = "税率必须大于0") @DecimalMax(value = "1.0", message = "税率不能超过100%") BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public @NotBlank(message = "SN码不能为空") String getSn() {
        return sn;
    }

    public void setSn(@NotBlank(message = "SN码不能为空") String sn) {
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

    public @Future(message = "保修截止日期必须是将来时间") LocalDate getWarrantyUntil() {
        return warrantyUntil;
    }

    public void setWarrantyUntil(@Future(message = "保修截止日期必须是将来时间") LocalDate warrantyUntil) {
        this.warrantyUntil = warrantyUntil;
    }
}
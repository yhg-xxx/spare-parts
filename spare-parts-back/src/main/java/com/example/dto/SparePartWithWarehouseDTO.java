package com.example.dto;

// SparePartWithWarehouseDTO.java
public interface SparePartWithWarehouseDTO {
    // 备件字段
    Integer getPartId();
    String getPartName();
    String getPartModel();
    String getCategory();
    String getSparePartStatus();
    String getSparePartType();
    String getSn();
    String getManufacturer();
    String getUnit();
    String getStatus();

    // 仓库字段（通过location_id关联）
    Integer getLocationId();       // 外键字段
    String getLocationName();      // 仓库名称
    String getLocationCode();      // 仓库编码（可选）
}
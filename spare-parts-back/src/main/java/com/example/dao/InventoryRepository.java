package com.example.dao;

import com.example.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer>, JpaSpecificationExecutor<Inventory> {
    // 找到对应库存
    Inventory findByPartNameAndLocationName(String partName, String locationName);
    // 原生SQL查询方式（MySQL示例）
    @Query(value = """
        SELECT * FROM inventory 
        WHERE 
            number REGEXP '^[0-9]+$'  -- 过滤有效数字
            AND CAST(number AS SIGNED) <= safety_stock
        """,
            nativeQuery = true)
    List<Inventory> findLowStockInventoryNative();
}

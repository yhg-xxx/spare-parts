package com.example.service;

import com.example.dao.InventoryRepository;
import com.example.entity.Inventory;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class InventoryService {

    // InventoryServiceImpl.java
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // 推荐使用原生查询方式
    public List<Inventory> getLowStockInventory() {
        return inventoryRepository.findLowStockInventoryNative();
    }
}

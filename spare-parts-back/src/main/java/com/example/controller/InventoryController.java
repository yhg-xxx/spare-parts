package com.example.controller;


import com.example.dao.InventoryRepository;
import com.example.entity.Inventory;
import com.example.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/inventory")
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    //根据order_id删除
    @DeleteMapping("/inventory/{inventoryId}")
    public Integer deleteInventory(@PathVariable int inventoryId) {
        try {
            inventoryRepository.deleteById(inventoryId);
            return 1; // 删除成功返回1
        } catch (EmptyResultDataAccessException e) {
            // 捕获删除不存在的记录异常
            return 0; // 删除失败返回0
        } catch (Exception e) {
            // 其他异常（如数据库连接失败）
            return 0;
        }
    }
    //根据order_id修改
    @PutMapping("/inventory/{inventoryId}")
    public Integer updateInventory(@PathVariable int inventoryId, @RequestBody Inventory inventory) {
        try {
            // 检查ID是否存在
            if (inventoryRepository.existsById(inventoryId)) {
                inventory.setInventoryId(inventoryId);
                inventoryRepository.save(inventory);
                return 1; // 更新成功返回1
            } else {
                return 0; // ID不存在返回0
            }
        } catch (Exception e) {
            return 0; // 其他异常
        }
    }
    //获取所有
    @GetMapping("/inventory/x")
    public List<Inventory> getAllInventorys() {
        return inventoryRepository.findAll();
    }

    // InventoryController.java
    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockInventory() {
        List<Inventory> list = inventoryService.getLowStockInventory();
        return ResponseEntity.ok(list);
    }
}


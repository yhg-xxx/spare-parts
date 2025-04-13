package com.example.controller;


import com.example.dao.InventoryRepository;
import com.example.entity.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;
    @PostMapping("/inventory")
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    //根据order_id删除
    @DeleteMapping("/inventory/{inventory_id}")
    public Integer deleteInventory(@PathVariable int inventory_id) {
        try {
            inventoryRepository.deleteById(inventory_id);
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
    @PutMapping("/inventory/{inventory_id}")
    public Integer updateInventory(@PathVariable int inventory_id, @RequestBody Inventory inventory) {
        try {
            // 检查ID是否存在
            if (inventoryRepository.existsById(inventory_id)) {
                inventory.setInventory_id(inventory_id);
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
}


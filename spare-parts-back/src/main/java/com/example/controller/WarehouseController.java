package com.example.controller;
import com.example.dao.WarehouseRepository;
import com.example.entity.Warehouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class WarehouseController {

    @Autowired
    private WarehouseRepository warehouseRepository;
    @PostMapping("/warehouse")
    public Warehouse createWarehouse(@RequestBody Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    //根据order_id删除
    @DeleteMapping("/warehouse/{location_id}")
    public Integer deleteWarehouse(@PathVariable int location_id) {
        try {
            warehouseRepository.deleteById(location_id);
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
    @PutMapping("/warehouse/{location_id}")
    public Integer updateWarehouse(@PathVariable int location_id, @RequestBody Warehouse warehouse) {
        try {
            // 检查ID是否存在
            if (warehouseRepository.existsById(location_id)) {
                warehouse.setLocation_id(location_id);
                warehouseRepository.save(warehouse);
                return 1; // 更新成功返回1
            } else {
                return 0; // ID不存在返回0
            }
        } catch (Exception e) {
            return 0; // 其他异常
        }
    }
    //获取所有
    @GetMapping("/warehouse/x")
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }
}


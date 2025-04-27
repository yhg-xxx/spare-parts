package com.example.controller;

import com.example.dao.Spare_partRepository;
import com.example.dto.SparePartWithWarehouseDTO;
import com.example.entity.Spare_part;
import com.example.entity.Warehouse;
import com.example.service.SparePartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class Spare_partController {
    @Autowired
    private Spare_partRepository spare_partRepository;
    //插入
    @PostMapping("/spare_part")
    public Spare_part createSpare_part(@RequestBody Spare_part spare_part) {
        return spare_partRepository.save(spare_part);
    }
    // 修改后的删除方法
    @DeleteMapping("/spare_part/{partId}")
    public ResponseEntity<?> deleteSpare_part(@PathVariable("partId") int partId) {
        try {
            spare_partRepository.deleteById(partId);
            return ResponseEntity.ok().body(Collections.singletonMap("success", true));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "备件不存在"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "服务器错误"));
        }
    }
    //根据order_id修改
    @PutMapping("/spare_part/{part_id}")
    public Integer updateSpare_part(@PathVariable("part_id") int part_id, @RequestBody Spare_part spare_part) {
        try {
            // 检查ID是否存在
            if (spare_partRepository.existsById(part_id)) {
                spare_part.setPartId(part_id);
                spare_partRepository.save(spare_part);
                return 1; // 更新成功返回1
            } else {
                return 0; // ID不存在返回0
            }
        } catch (Exception e) {
            return 0; // 其他异常
        }
    }

    @Autowired
    private SparePartService sparePartService;

    // SparePartController.java
    @GetMapping("/spare_part")
    public ResponseEntity<Page<SparePartWithWarehouseDTO>> getSpareParts(
            @RequestParam(required = false) String partName,
            @RequestParam(defaultValue = "0") int page, // 接收前端传来的page-1
            @RequestParam(defaultValue = "10") int size) { // 参数名保持size
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(sparePartService.searchParts(partName, pageable));
    }

    //获取所有
    @GetMapping("/spare_part/x")
    public List<Spare_part> getAllspart() {
        return spare_partRepository.findAll();
    }

}
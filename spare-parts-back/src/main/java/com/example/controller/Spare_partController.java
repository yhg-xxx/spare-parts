package com.example.controller;

import com.example.dao.Spare_partRepository;
import com.example.entity.Spare_part;
import com.example.service.SparePartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    //根据part_id删除
    @DeleteMapping("/spare_part/{part_id}")
    public Integer deleteSpare_part(@PathVariable("part_id") int part_id) {
        try {
            spare_partRepository.deleteById(part_id);
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

    @GetMapping("/spare_part")
    public ResponseEntity<Page<Spare_part>> getSpareParts(
            @RequestParam(required = false) String partName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Spare_part> result = sparePartService.searchParts(partName, pageable);
        return ResponseEntity.ok(result);
    }
    //获取所有
    @GetMapping("/spare_part/x")
    public List<Spare_part> getAllSpare_parts() {
        return spare_partRepository.findAll();
    }

}
package com.example.controller;

import com.example.dao.*;
import com.example.dto.SparePartWithWarehouseDTO;
import com.example.entity.*;
import com.example.service.SparePartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class Spare_partController {
    @Autowired
    private Spare_partRepository spare_partRepository;
    @Autowired
    private InboundRecordRepository inboundRecordRepository;

    @Autowired
    private FaultOrderRepository faultOrderRepository;

    @Autowired
    private StockoutRepository stockoutRepository;

    @Autowired
    private TransferRecordRepository transferRecordRepository;
    @Autowired
    private ReturnFactoryRepository returnFactoryRepository;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(sparePartService.searchParts(partName, pageable));
    }
    @GetMapping("/spare_part/x")
    public List<Spare_part> getAllspart() {
        return spare_partRepository.findAll();
    }
    // Spare_partController.java 新增方法
    @PutMapping("/spare_part/{part_id}/status")
    public ResponseEntity<Map<String, Object>> updateSparePartStatus(
            @PathVariable("part_id") int partId,
            @RequestBody Map<String, String> statusUpdate) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 获取备件信息
            Optional<Spare_part> optionalPart = spare_partRepository.findById(partId);
            if (!optionalPart.isPresent()) {
                response.put("code", 404);
                response.put("message", "备件不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 2. 校验状态值
            String newStatus = statusUpdate.get("status");
            if (!List.of("在库", "已出库").contains(newStatus)) {
                response.put("code", 400);
                response.put("message", "无效状态值，允许值：[在库, 已出库]");
                return ResponseEntity.badRequest().body(response);
            }

            // 3. 更新状态
            Spare_part part = optionalPart.get();
            part.setStatus(newStatus); // 仅更新状态字段
            spare_partRepository.save(part);

            response.put("code", 200);
            response.put("message", "状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "服务器内部错误");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    // 添加在 Spare_partController 类中
    @GetMapping("/spare_part/bySn/{sn}")
    public ResponseEntity<?> getBySn(@PathVariable String sn) {
        try {
            Optional<Spare_part> part = spare_partRepository.findBySn(sn);
            if (part.isPresent()) {
                return ResponseEntity.ok(part.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "SN码不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "服务器错误"));
        }
    }
    @GetMapping("/lifecycle/{sn}")
    public ResponseEntity<Map<String, Object>> getLifecycleInfo(@PathVariable String sn) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 获取基础信息
        Spare_part part = spare_partRepository.findBySn(sn)
                .orElseThrow(() -> new ResourceNotFoundException("Spare part not found"));
        result.put("basicInfo", part);

        // 2. 获取入库信息
        inboundRecordRepository.findBySn(sn).ifPresent(
                inbound -> result.put("inboundInfo", inbound)
        );

        // 3. 故障维修记录
        List<FaultOrder> faults = faultOrderRepository.findBySn(sn);
        if (!faults.isEmpty()) {
            result.put("faultRecords", faults);
        }

        // 4. 出库记录
        List<Stockout> stockouts = stockoutRepository.findBySn(sn);
        if (!stockouts.isEmpty()) {
            result.put("stockoutRecords", stockouts);
        }

        // 5. 调拨记录
        List<TransferRecord> transfers = transferRecordRepository.findBySn(sn);
        if (!transfers.isEmpty()) {
            result.put("transferRecords", transfers);
        }
        // 6. 返厂记录
        List<ReturnFactoryRecord> ReturnFactoryRecords = returnFactoryRepository.findBySn(sn);
        if (!ReturnFactoryRecords.isEmpty()) {
            result.put("returnFactoryRecords", ReturnFactoryRecords);
        }


        return ResponseEntity.ok(result);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
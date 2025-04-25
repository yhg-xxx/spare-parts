package com.example.controller;

import com.example.dao.FaultOrderRepository;
import com.example.entity.FaultOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fault-orders")
public class FaultOrderController {

    @Autowired
    private FaultOrderRepository faultOrderRepository;

    // 查询所有故障工单
    @GetMapping
    public ResponseEntity<?> getAllFaultOrders() {
        List<FaultOrder> orders = faultOrderRepository.findAll();
        return ResponseEntity.ok(orders); // HTTP 200
    }

    // 新增故障工单
    @PostMapping
    public ResponseEntity<?> createFaultOrder(@RequestBody FaultOrder newOrder) {
        try {
            FaultOrder savedOrder = faultOrderRepository.save(newOrder);
            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED); // HTTP 201
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建失败: " + e.getMessage()); // HTTP 400
        }
    }
    // 查询单个故障工单详情
    @GetMapping("/{faultId}")
    public ResponseEntity<?> getFaultOrderDetail(@PathVariable Long  faultId) {
        Optional<FaultOrder> order = faultOrderRepository.findById(faultId);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    //根据order_id修改
    @PutMapping("/{faultId}")
    public Integer updateFaultOrder(@PathVariable Long  faultId, @RequestBody FaultOrder faultOrder) {
        try {
            // 检查ID是否存在
            if (faultOrderRepository.existsById(faultId)) {
                faultOrder.setFaultId(faultId);
                faultOrderRepository.save(faultOrder);
                return 1; // 更新成功返回1
            } else {
                return 0; // ID不存在返回0
            }
        } catch (Exception e) {
            return 0; // 其他异常
        }
    }
    // 新增接单接口
    @PutMapping("/{faultId}/accept")
    public ResponseEntity<?> acceptOrder(
            @PathVariable Long faultId,
            @RequestBody Map<String, String> request) {

        try {
            // 1. 获取工单信息
            Optional<FaultOrder> optionalOrder = faultOrderRepository.findById(faultId);
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            FaultOrder order = optionalOrder.get();

            // 2. 验证状态流转
            if (!order.getWorkOrderStatus().equals(FaultOrder.WorkOrderStatus.待处理)) {
                return ResponseEntity.badRequest()
                        .body("只有待处理状态工单可接单");
            }

            // 3. 更新接单信息
            order.setWorkOrderStatus(FaultOrder.WorkOrderStatus.处理中);
            order.setRepairBy(request.get("repairBy"));
            order.setProcessedAt(LocalDateTime.now());

            // 4. 保存更新
            FaultOrder updatedOrder = faultOrderRepository.save(order);

            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("接单失败: " + e.getMessage());
        }
    }
}
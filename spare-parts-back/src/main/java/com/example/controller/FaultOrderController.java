package com.example.controller;

import com.example.dao.FaultOrderRepository;
import com.example.dao.Spare_partRepository;
import com.example.entity.FaultOrder;
import com.example.entity.Spare_part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fault-orders")
public class FaultOrderController {

    @Autowired
    private Spare_partRepository sparePartRepository;
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
    // FaultOrderController.java
    @PutMapping("/{faultId}")
    public ResponseEntity<?> updateFaultOrder(
            @PathVariable Long faultId,
            @RequestBody Map<String, Object> updates) {

        try {
            Optional<FaultOrder> optionalOrder = faultOrderRepository.findById(faultId);
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FaultOrder existingOrder = optionalOrder.get();
            // 添加上下文没有的字段处理
            if (updates.containsKey("disposalType")) {
                existingOrder.setDisposalType(FaultOrder.DisposalType.valueOf((String) updates.get("disposalType")));
            }
            if (updates.containsKey("reviewResult") && updates.containsKey("disposalType")) {
                updateSparePartStatus(existingOrder);
            }
            if (updates.containsKey("reviewBy")) {
                existingOrder.setReviewBy((String) updates.get("reviewBy"));
            }
            if (updates.containsKey("reviewAt")) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
                Instant instant = Instant.from(formatter.parse((String) updates.get("reviewAt")));
                existingOrder.setReviewAt(instant.atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            // 更新工单状态
            if (updates.containsKey("workOrderStatus")) {
                existingOrder.setWorkOrderStatus(
                        FaultOrder.WorkOrderStatus.valueOf(
                                (String) updates.get("workOrderStatus")
                        )
                );
            }

            // 更新维修结果（图片URL）
            if (updates.containsKey("repairResult")) {
                existingOrder.setRepairResult((String) updates.get("repairResult"));
            }

            FaultOrder updatedOrder = faultOrderRepository.save(existingOrder);
            return ResponseEntity.ok(updatedOrder);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("更新失败: " + e.getMessage());
        }
    }
    // 新增备件状态更新方法
    private void updateSparePartStatus(FaultOrder order) {
        if (order.getSn() == null || order.getSn().isEmpty()) {
            throw new IllegalArgumentException("SN号不能为空");
        }

        Spare_part sparePart = sparePartRepository.findBySn(order.getSn())
                .orElseThrow(() -> new RuntimeException("未找到对应SN号的备件"));

        switch (order.getDisposalType()) {
            case 返厂修:
                sparePart.setSparePartStatus(Spare_part.SparePartStatus.返厂修);
                break;
            case 修好件:
                sparePart.setSparePartStatus(Spare_part.SparePartStatus.修好件);
                break;
            case 报废:
                sparePart.setSparePartStatus(Spare_part.SparePartStatus.已报废);
                break;
            default:
                throw new IllegalStateException("未知处置类型: " + order.getDisposalType());
        }

        sparePartRepository.save(sparePart);
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
    // 新增流程跟踪接口
    @GetMapping("/{faultId}/timeline")
    public ResponseEntity<?> getProcessTimeline(@PathVariable Long faultId) {
        Optional<FaultOrder> optionalOrder = faultOrderRepository.findById(faultId);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FaultOrder order = optionalOrder.get();
        List<Map<String, Object>> timeline = new ArrayList<>();

        // 基础创建记录
        timeline.add(createTimelineNode("工单创建", order.getCreatedAt(), "系统自动生成"));

        // 处理中记录
        if (order.getProcessedAt() != null) {
            timeline.add(createTimelineNode("开始处理", order.getProcessedAt(), order.getRepairBy()));
        }

        // 验收记录
        if (order.getReviewAt() != null) {
            String action = order.getReviewResult() == FaultOrder.ReviewResult.通过 ? "验收通过" : "验收驳回";
            timeline.add(createTimelineNode(action, order.getReviewAt(), order.getReviewBy()));
        }

        // 最终处置记录
        if (order.getWorkOrderStatus() == FaultOrder.WorkOrderStatus.已关闭) {
            String action = "";
            if (order.getDisposalType() != null) {
                action = switch (order.getDisposalType()) {
                    case 返厂修 -> "返厂维修完成";
                    case 修好件 -> "现场维修完成";
                    case 报废 -> "备件已报废";
                };
            }
            if (!action.isEmpty()) {
                timeline.add(createTimelineNode(action, LocalDateTime.now(), order.getReviewBy()));
            }
        }

        return ResponseEntity.ok(timeline);
    }

    private Map<String, Object> createTimelineNode(String status, LocalDateTime time, String operator) {
        return Map.of(
                "status", status,
                "time", time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "operator", operator != null ? operator : "系统"
        );
    }
}
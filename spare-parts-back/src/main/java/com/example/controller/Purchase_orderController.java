package com.example.controller;

import com.example.config.ResourceNotFoundException;
import com.example.dao.Purchase_orderRepository;
import com.example.dao.StatusHistoryRepository;
import com.example.dao.UserRepository;
import com.example.dto.StatusHistoryDTO;
import com.example.entity.PurchaseOrderStatusHistory;
import com.example.entity.Purchase_order;
import com.example.entity.User;
import com.example.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class Purchase_orderController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Purchase_orderRepository purchase_orderRepository;
    @Autowired
    private StatusHistoryRepository statusHistoryRepository;
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    //插入
    @PostMapping("/purchase_order")
    public Purchase_order createPurchase_order(@RequestBody Purchase_order purchase_order) {
        purchase_order.setCreated_at(LocalDateTime.now().toString());
        return purchase_orderRepository.save(purchase_order);
    }

    //获取待入库的采购单
    @GetMapping("/purchase_order/s")
    public List<Purchase_order> getPendingPurchaseOrders() {
        return purchase_orderRepository.findByStatus("已完成");
    }
    @GetMapping("/purchase_order/a")
    public List<Purchase_order> getPurchaseOrders(@RequestParam(required = false) String spare_part_name) {
        return purchaseOrderService.findBySparePartName(spare_part_name);
    }
    @PutMapping("/purchase_order/{orderId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> payload) {

        String newStatus = (String) payload.get("newStatus");
        Integer operatorId = (Integer) payload.get("operatorId");

        // 1. 更新订单状态
        Purchase_order order = purchase_orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(newStatus);
        if ("已完成".equals(newStatus)) {
            order.setCompleted_at(LocalDateTime.now().toString());
        }
        purchase_orderRepository.save(order);
// 在方法内获取实体
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // 2. 记录状态变更历史
        PurchaseOrderStatusHistory history = new PurchaseOrderStatusHistory();
        history.setPurchaseOrder(order);// 使用完整的订单对象
        history.setStatus(newStatus);
        history.setChangedBy(operator); // 使用完整的用户对象
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        return ResponseEntity.ok().build();


    }

    @GetMapping("/purchase_order/{orderId}/histories")
    public ResponseEntity<List<StatusHistoryDTO>> getStatusHistories(
            @PathVariable Integer orderId) {

        Purchase_order order = purchase_orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // 获取数据库记录
        List<PurchaseOrderStatusHistory> histories =
                statusHistoryRepository.findByPurchaseOrderOrderByChangedAtAsc(order);

        // 创建初始状态（待上会）
        StatusHistoryDTO initialStatus = new StatusHistoryDTO(
                "待上会",
                LocalDateTime.parse(order.getCreated_at()), // 需确保created_at是LocalDateTime格式
                userRepository.findById(order.getApplicant_id())
                        .orElseThrow().getName()
        );

        // 合并数据
        List<StatusHistoryDTO> dtos = histories.stream()
                .map(history -> new StatusHistoryDTO(
                        history.getStatus(),
                        history.getChangedAt(),
                        history.getChangedBy().getName()
                )).collect(Collectors.toList());

        // 插入初始状态到列表开头
        dtos.add(0, initialStatus);

        return ResponseEntity.ok(dtos);
    }
}

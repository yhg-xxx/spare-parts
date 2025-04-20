package com.example.controller;

import com.example.dao.Purchase_orderRepository;
import com.example.entity.Purchase_order;
import com.example.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Purchase_orderController {
    @Autowired
    private Purchase_orderRepository purchase_orderRepository;
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    //插入
    @PostMapping("/purchase_order")
    public Purchase_order createPurchase_order(@RequestBody Purchase_order purchase_order) {
        return purchase_orderRepository.save(purchase_order);
    }

    //获取待入库的采购单
    @GetMapping("/purchase_order/s")
    public List<Purchase_order> getPendingPurchaseOrders() {
        return purchase_orderRepository.findByStatus("待入库");
    }
    @GetMapping("/purchase_order/a")
    public List<Purchase_order> getPurchaseOrders(@RequestParam(required = false) String spare_part_name) {
        return purchaseOrderService.findBySparePartName(spare_part_name);
    }
    //根据order_id删除
    @DeleteMapping("/purchase_order/{order_id}")
    public Integer deletePurchase_order(@PathVariable int order_id) {
        try {
            purchase_orderRepository.deleteById(order_id);
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
    @PutMapping("/purchase_order/{order_id}")
    public Integer updatePurchase_order(@PathVariable int order_id, @RequestBody Purchase_order purchase_order) {
        try {
            // 检查ID是否存在
            if (purchase_orderRepository.existsById(order_id)) {
                purchase_order.setOrder_id(order_id);
                purchase_orderRepository.save(purchase_order);
                return 1; // 更新成功返回1
            } else {
                return 0; // ID不存在返回0
            }
        } catch (Exception e) {
            return 0; // 其他异常
        }
    }
//获取所有
    @GetMapping("/purchase_order/x")
    public List<Purchase_order> getAllPurchase_orders() {
        return purchase_orderRepository.findAll();
    }
}

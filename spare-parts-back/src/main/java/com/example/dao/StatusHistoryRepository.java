package com.example.dao;

import com.example.entity.PurchaseOrderStatusHistory;

import com.example.entity.Purchase_order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<PurchaseOrderStatusHistory, Integer> {
    // 正确命名：通过采购订单对象查询
    List<PurchaseOrderStatusHistory> findByPurchaseOrderOrderByChangedAtAsc(Purchase_order order);
}

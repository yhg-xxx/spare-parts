package com.example.dao;

import com.example.entity.InboundRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboundRecordRepository extends JpaRepository<InboundRecord, Integer> {
    // 根据采购订单ID查询
    List<InboundRecord> findByOrderId(Integer orderId);


}

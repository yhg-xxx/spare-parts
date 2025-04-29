package com.example.dao;

import com.example.entity.InboundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InboundRecordRepository extends
        JpaRepository<InboundRecord, Integer>,
        JpaSpecificationExecutor<InboundRecord> { // 添加这个接口

    // 根据采购订单ID查询
    List<InboundRecord> findByOrderId(Integer orderId);


    Optional<Object> findBySn(String sn);
}

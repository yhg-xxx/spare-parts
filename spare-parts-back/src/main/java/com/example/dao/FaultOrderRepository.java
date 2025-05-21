package com.example.dao;

import com.example.entity.FaultOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaultOrderRepository extends
        JpaRepository<FaultOrder, Long>,
        JpaSpecificationExecutor<FaultOrder> {
    List<FaultOrder> findBySn(String sn);

    List<FaultOrder> findBySnAndWorkOrderStatus(String sn, String WorkOrderStatus);
}

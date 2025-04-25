package com.example.dao;

import com.example.entity.FaultOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FaultOrderRepository extends
        JpaRepository<FaultOrder, Long>,
        JpaSpecificationExecutor<FaultOrder> {
}

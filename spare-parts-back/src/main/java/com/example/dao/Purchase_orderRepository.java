package com.example.dao;

import com.example.entity.Purchase_order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Purchase_orderRepository extends JpaRepository<Purchase_order, Integer> , JpaSpecificationExecutor<Purchase_order> {
    List<Purchase_order> findByStatus(String status);
}
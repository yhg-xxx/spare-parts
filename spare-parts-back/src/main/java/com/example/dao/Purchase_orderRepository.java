package com.example.dao;

import com.example.entity.Purchase_order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Purchase_orderRepository extends JpaRepository<Purchase_order, Integer> {
}

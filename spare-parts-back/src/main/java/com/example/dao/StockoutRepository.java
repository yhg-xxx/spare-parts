package com.example.dao;


import com.example.entity.Stockout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockoutRepository extends JpaRepository<Stockout, Integer> {

    Page<Stockout> findByLocationName(String locationName, Pageable pageable);
    // StockoutRepository.java 新增方法
    List<Stockout> findByStatus(String status);
    // 添加这两个方法


    List<Stockout> findByStatusIn(List<String> statuses);
}



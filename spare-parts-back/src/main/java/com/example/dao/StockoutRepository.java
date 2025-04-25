package com.example.dao;


import com.example.entity.Stockout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockoutRepository extends JpaRepository<Stockout, Integer> {

    Page<Stockout> findByLocationName(String locationName, Pageable pageable);
}



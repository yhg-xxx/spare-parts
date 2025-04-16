package com.example.dao;

import com.example.entity.Spare_part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Spare_partRepository extends JpaRepository<Spare_part, Integer> {
    Page<Spare_part> findByPartNameContaining(String partName, Pageable pageable);
}
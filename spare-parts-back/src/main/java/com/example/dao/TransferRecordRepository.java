package com.example.dao;

import com.example.entity.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, Integer> {

}
package com.example.dao;

import com.example.entity.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, Integer> {

    List<TransferRecord> findBySn(String sn);
}
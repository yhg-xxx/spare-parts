package com.example.dao;

import com.example.entity.ScrapRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRecordRepository extends JpaRepository<ScrapRecord, Integer> {
    List<ScrapRecord> findByApplicantId(String applicantId);
    List<ScrapRecord> findByPartStatus(ScrapRecord.PartStatus partStatus);

    Optional<Object> findBySn(String sn);
}
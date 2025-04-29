package com.example.dao;

import com.example.entity.ReturnFactoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnFactoryRepository extends JpaRepository<ReturnFactoryRecord, Integer>, JpaSpecificationExecutor<ReturnFactoryRecord> {

    @Query("SELECT r FROM ReturnFactoryRecord r WHERE r.sn = :sn")
    List<ReturnFactoryRecord> findBySn(@Param("sn") String sn);

    @Query("SELECT r FROM ReturnFactoryRecord r WHERE r.partId = :partId")
    List<ReturnFactoryRecord> findByPartId(@Param("partId") Integer partId);

    @Query("SELECT r FROM ReturnFactoryRecord r WHERE r.status = :status")
    List<ReturnFactoryRecord> findByStatus(@Param("status") String status);

    @Query("SELECT r FROM ReturnFactoryRecord r WHERE r.faultId = :faultId")
    Optional<ReturnFactoryRecord> findByFaultId(@Param("faultId") Long faultId);
}
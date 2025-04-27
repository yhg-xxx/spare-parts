package com.example.dao;

import com.example.dto.SparePartWithWarehouseDTO;
import com.example.entity.Spare_part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Spare_partRepository extends JpaRepository<Spare_part, Integer> {
    @Query("""
        SELECT 
            s.partId as partId,
            s.partName as partName,
            s.partModel as partModel,
            s.category as category,
            s.sparePartStatus as sparePartStatus,
            s.sparePartType as sparePartType,
            s.sn as sn,
            s.manufacturer as manufacturer,
            s.unit as unit,
            s.locationId as locationId,
            w.locationName as locationName,
            w.location_code as locationCode
        FROM Spare_part s
        LEFT JOIN Warehouse w ON s.locationId = w.location_id
        WHERE (:partName IS NULL OR s.partName LIKE %:partName%)
    """)
    Page<SparePartWithWarehouseDTO> searchWithWarehouse(
            @Param("partName") String partName,
            Pageable pageable
    );
    Optional<Spare_part> findBySn(String sn);
    @Query("SELECT s.sn FROM Spare_part s WHERE s.locationId = :locationId AND s.partName = :partName AND s.sparePartStatus IN (com.example.entity.Spare_part.SparePartStatus.新好件, com.example.entity.Spare_part.SparePartStatus.修好件)")
    List<String> findSnByLocationIdAndPartName(
            @Param("locationId") Integer locationId,
            @Param("partName") String partName);

}
package com.example.dao;

import com.example.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public  interface  WarehouseRepository extends JpaRepository<Warehouse, Integer>,JpaSpecificationExecutor<Warehouse> {
    Optional<Warehouse> findByLocationName(String toLocationName);

    // 添加@Param注解明确参数绑定
    @Query("SELECT COUNT(w) > 0 FROM Warehouse w WHERE w.locationName = :name")
    boolean existsByLocationName(String name);
}

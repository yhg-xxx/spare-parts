package com.example.dao;

import com.example.entity.UsageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsageRequestRepository extends JpaRepository<UsageRequest, Integer> {


}
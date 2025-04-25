package com.example.dao;

import com.example.entity.UsageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface UsageRequestRepository extends JpaRepository<UsageRequest, Integer> {

    boolean existsAllByIdIn(Set<Integer> requestIds);
}
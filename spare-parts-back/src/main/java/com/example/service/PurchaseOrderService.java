package com.example.service;

import com.example.dao.Purchase_orderRepository;
import com.example.entity.Purchase_order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


// PurchaseOrderService.java
@Service
public class PurchaseOrderService {

    @Autowired
    private Purchase_orderRepository purchaseOrderRepository;

    public List<Purchase_order> findBySparePartName(String spare_part_name) {
        return purchaseOrderRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(spare_part_name)) {
                // 添加前后通配符实现模糊查询
                String likePattern = "%" + spare_part_name + "%";
                // 使用不区分大小写的模糊查询
                predicates.add(cb.like(
                        cb.lower(root.get("spare_part_name")),
                        likePattern.toLowerCase()
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}
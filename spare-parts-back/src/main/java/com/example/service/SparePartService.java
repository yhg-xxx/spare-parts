package com.example.service;

import com.example.dao.Spare_partRepository;
import com.example.entity.Spare_part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class SparePartService {
    @Autowired
    private Spare_partRepository sparePartRepository;

    public Page<Spare_part> searchParts(String partName, Pageable pageable) {
        return sparePartRepository.findByPartNameContaining(partName, pageable);
    }
}
package com.example.service;// StockoutService.java
import com.example.dao.StockoutRepository;
import com.example.dao.UsageRequestRepository;
import com.example.entity.Stockout;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StockoutService {

    private final StockoutRepository stockoutRepository;
    private final UsageRequestRepository usageRequestRepository;

    public StockoutService(StockoutRepository stockoutRepository,
                           UsageRequestRepository usageRequestRepository) {
        this.stockoutRepository = stockoutRepository;
        this.usageRequestRepository = usageRequestRepository;
    }

    /**
     * 创建出库记录（自动触发填充备件信息）
     */
    @Transactional
    public Stockout createStockout(Stockout stockout) {
        // 验证关联领用单是否存在
        if (stockout.getRequestId() != null &&
                !usageRequestRepository.existsById(stockout.getRequestId())) {
            throw new RuntimeException("关联领用单不存在");
        }

        // 数据库触发器会自动填充 part_name 和 part_model
        return stockoutRepository.save(stockout);
    }

    /**
     * 获取分页列表（带仓库过滤）
     */
    public Page<Stockout> getStockouts(String locationName, Pageable pageable) {
        if (locationName != null) {
            return stockoutRepository.findByLocationName(locationName, pageable);
        }
        return stockoutRepository.findAll(pageable);
    }

    /**
     * 更新出库状态
     */
    @Transactional
    public void updateStatus(Integer id, String status) {
        Stockout stockout = stockoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("出库记录不存在"));
        stockout.setStatus(status);
        stockoutRepository.save(stockout);
    }

    /**
     * 获取单个出库记录详情
     */
    public Stockout getStockoutDetail(Integer id) {
        return stockoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("出库记录不存在"));
    }
    @Transactional
    public List<Stockout> createBatch(List<Stockout> stockouts) {
        // 验证领用单ID有效性
        Set<Integer> requestIds = stockouts.stream()
                .map(Stockout::getRequestId)
                .collect(Collectors.toSet());

        if (!usageRequestRepository.existsAllByIdIn(requestIds)) {
            throw new RuntimeException("包含无效的领用单ID");
        }

        return stockoutRepository.saveAll(stockouts);
    }

}
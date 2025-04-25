package com.example.controller;

import com.example.dao.StockoutRepository;
import com.example.dao.UserRepository;
import com.example.entity.Stockout;
import com.example.service.StockoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/stockouts")
public class StockoutController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StockoutRepository stockoutRepository;

    private final StockoutService stockoutService;

    public StockoutController(StockoutService stockoutService) {
        this.stockoutService = stockoutService;
    }

    /**
     * 创建出库单
     */
    @PostMapping
    public ResponseEntity<Stockout> create(@RequestBody Stockout stockout) {
        return ResponseEntity.ok(stockoutService.createStockout(stockout));
    }


    @GetMapping
    public ResponseEntity<Page<Stockout>> list(
            @RequestParam(required = false) String locationName,
            @PageableDefault(sort = "outTime", direction = DESC) Pageable pageable) {
        return ResponseEntity.ok(stockoutService.getStockouts(locationName, pageable));
    }

    /**
     * 查看出库单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Stockout> detail(@PathVariable Integer id) {
        return ResponseEntity.ok(stockoutService.getStockoutDetail(id));
    }

    /**
     * 更新出库状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        stockoutService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/batch")
    public ResponseEntity<List<Stockout>> createBatch(@RequestBody List<Stockout> stockouts) {
        // 验证操作员有效性
        stockouts.forEach(stockout -> {
            if (!userRepository.existsById(stockout.getOperatorId())) {
                throw new RuntimeException("无效操作员ID: " + stockout.getOperatorId());
            }
        });
        return ResponseEntity.ok(stockoutService.createBatch(stockouts));
    }

    // StockoutController.java
    @GetMapping("/all")
    public ResponseEntity<List<Stockout>> getAllStockouts() {
        return ResponseEntity.ok(stockoutRepository.findAll());
    }

}


package com.example.controller;

import com.example.dao.InboundRecordRepository;
import com.example.dao.Purchase_orderRepository;
import com.example.dto.InboundBatchCreateRequest;
import com.example.dto.InboundCreateRequest;
import com.example.dto.InboundWithPurchaseDTO;
import com.example.entity.InboundRecord;

import com.example.entity.Purchase_order;
import com.example.service.InboundRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inbound-records")
public class InboundRecordController {

    private final InboundRecordService service;
    @Autowired
    private InboundRecordRepository inboundRecordRepository;
    @Autowired
    private Purchase_orderRepository purchaseOrderRepository;
    public InboundRecordController(InboundRecordService service) {
        this.service = service;
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<InboundRecord> createInboundRecords(
            @RequestBody @Valid InboundBatchCreateRequest request) { // 改为接收批量请求

        return service.createInboundRecords(request.getOrderId(), request.getRequests());
    }

    @GetMapping("/with-purchase")
    public Page<InboundWithPurchaseDTO> getAllWithPurchase(
            @RequestParam(required = false) String spare_part_name,
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return service.findAllWithPurchaseOrders(
                spare_part_name,
                sn,
                startTime,
                endTime,
                page,
                size
        );
    }
    //查看详情
    @GetMapping("/{inboundId}/with-purchase")
    public InboundWithPurchaseDTO getWithPurchaseById(
            @PathVariable Integer inboundId) {
        return service.findWithPurchaseOrderById(inboundId);
    }
    //获取所有
    @GetMapping("/x")
    public List<InboundRecord> getAllPurchase_orders() {
        return inboundRecordRepository.findAll();
    }


    @PutMapping("/{inboundId}")
    public Integer updateinboundId(@PathVariable int inboundId, @RequestBody InboundRecord inboundRecord) {
        try {
            // 检查ID是否存在
            if (inboundRecordRepository.existsById(inboundId)) {
                inboundRecord.setInboundId(inboundId);
                inboundRecordRepository.save(inboundRecord);
                return 1; // 更新成功返回1
            } else {
                return 0; // ID不存在返回0
            }
        } catch (Exception e) {
            return 0; // 其他异常
        }
    }
    // 新增：查询参数版（可选）
    @GetMapping(params = "orderId")
    public List<InboundRecord> getByOrderIdParam(@RequestParam Integer orderId) {
        return service.findByOrderId(orderId);
    }

    // 修改：删除端点参数名明确
    @DeleteMapping("/{inboundId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("inboundId") Integer id) {
        service.delete(id);
    }

    // 异常处理
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(InboundRecordService.ResourceNotFoundException.class)
    public Map<String, String> handleNotFound(Exception ex) {
        return Map.of("message", ex.getMessage());
    }


}
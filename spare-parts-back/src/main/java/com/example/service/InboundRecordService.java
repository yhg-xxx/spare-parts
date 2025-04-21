package com.example.service;

import com.example.config.BusinessException;
import com.example.dao.*;
import com.example.dto.InboundCreateRequest;
import com.example.dto.InboundWithPurchaseDTO;
import com.example.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InboundRecordService {
    private final Spare_partRepository sparePartRepository;
    private final InboundRecordRepository repository;
    private final Purchase_orderRepository purchaseOrderRepository; // 新增
    public InboundRecordService(InboundRecordRepository repository,
    Purchase_orderRepository purchaseOrderRepository,Spare_partRepository sparePartRepository) {
        this.repository = repository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.sparePartRepository = sparePartRepository;
    }
    public InboundWithPurchaseDTO findWithPurchaseOrderById(Integer inboundId) {
        InboundRecord inbound = repository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("入库记录不存在"));

        Purchase_order order = purchaseOrderRepository
                .findById(inbound.getOrderId())
                .orElse(null);

        return new InboundWithPurchaseDTO(inbound, order);
    }

    public Page<InboundWithPurchaseDTO> findAllWithPurchaseOrders(
            String sparePartName,
            String sn,
            String startTime,
            String endTime,
            int page,
            int size) {

        // 创建分页请求（注意Spring Data页码从0开始）
        Pageable pageable = PageRequest.of(page, size);

        // 构建查询条件
        Specification<InboundRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // SN号过滤
            if (sn != null && !sn.isEmpty()) {
                predicates.add(cb.like(root.get("sn"), "%" + sn + "%"));
            }

            // 时间范围过滤
            if (startTime != null) {
                LocalDateTime start = parseLocalDateTime(startTime + " 00:00:00");
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (endTime != null) {
                LocalDateTime end = parseLocalDateTime(endTime + " 23:59:59");
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }

            // 关联采购单的备件名称过滤
            if (sparePartName != null && !sparePartName.isEmpty()) {
                Join<InboundRecord, Purchase_order> orderJoin = root.join("purchaseOrder"); // 使用实体关联属性名
                predicates.add(cb.like(orderJoin.get("spare_part_name"), "%" + sparePartName + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 执行分页查询
        Page<InboundRecord> inboundPage = repository.findAll(spec, pageable);

        // 转换DTO（保持原有逻辑）
        List<InboundWithPurchaseDTO> content = inboundPage.getContent().stream()
                .map(inbound -> {
                    Purchase_order order = purchaseOrderRepository.findById(inbound.getOrderId())
                            .orElse(new Purchase_order());
                    return new InboundWithPurchaseDTO(inbound, order);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, inboundPage.getTotalElements());
    }
    // 新的日期解析方法
    private LocalDateTime parseLocalDateTime(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }
    // 新增：根据订单ID查询记录
    public List<InboundRecord> findByOrderId(Integer orderId) {
        return repository.findByOrderId(orderId);
    }

    // 删除方法参数明确为inboundId
    @Transactional
    public void delete(Integer inboundId) {  // 参数名更明确
        if (!repository.existsById(inboundId)) {
            throw new ResourceNotFoundException("入库记录不存在");
        }
        repository.deleteById(inboundId);
    }

    public List<InboundRecord> createInboundRecords(Integer orderId, List<InboundCreateRequest> requests) {
        Purchase_order po = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("采购订单不存在"));

        // 解析采购订单总数量
        int purchaseQty;
        try {
            purchaseQty = Integer.parseInt(po.getNumber().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("采购单数量格式错误");
        }

        // 验证入库记录条数与采购数量一致（假设每条记录代表一件商品）
        if (requests.size() != purchaseQty) {
            throw new IllegalArgumentException(
                    String.format("入库数量不符：采购单%d件，当前提交%d件",
                            purchaseQty, requests.size())
            );
        }

        List<InboundRecord> records = requests.stream().map(request -> {
            syncSparePart(po, request);
            InboundRecord record = new InboundRecord();
            // 设置基础字段
            record.setOrderId(orderId);
            record.setLocationId(request.getLocationId());
            record.setSn(request.getSn());
            record.setSparePartCategory(InboundRecord.SparePartCategory.valueOf(request.getSparePartCategory()));
            record.setSparePartStatus(InboundRecord.SparePartStatus.valueOf(request.getSparePartStatus()));
            record.setUnit(request.getUnit());
            record.setManufacturer(request.getManufacturer());
            record.setWarrantyUntil(request.getWarrantyUntil());

            // 财务计算（直接使用采购订单总数量）
            BigDecimal unitPrice = request.getUnitPrice();
            BigDecimal taxRate = request.getTaxRate();
            BigDecimal quantity = BigDecimal.valueOf(purchaseQty); // 关键修改：使用采购总量

            // 计算单件税额及总金额
            BigDecimal taxFreeUnitPrice = unitPrice.divide(
                    BigDecimal.ONE.add(taxRate), 4, RoundingMode.HALF_UP
            ).setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxPerUnit = unitPrice.subtract(taxFreeUnitPrice);

            // 乘以总数量
            record.setUnitPrice(unitPrice);
            record.setTaxRate(taxRate);
            record.setTaxFreeUnitPrice(taxFreeUnitPrice);
            record.setTaxAmount(taxPerUnit);
            record.setTaxFreeTotal(taxFreeUnitPrice.multiply(quantity)); // 总不含税金额
            record.setTotalTax(taxPerUnit.multiply(quantity));          // 总税额

            return record;
        }).collect(Collectors.toList());

        List<InboundRecord> savedRecords = repository.saveAll(records);

        // 更新采购订单状态和完成时间
        po.setStatus("已入库");
        purchaseOrderRepository.save(po);
        return savedRecords;
    }

    private void syncSparePart(Purchase_order purchaseOrder, InboundCreateRequest request) {
        // 1. 校验SN号必填
        if (request.getSn() == null || request.getSn().isBlank()) {
            throw new IllegalArgumentException("SN号不能为空");
        }

        // 2. 根据SN号查询备件
        Optional<Spare_part> existing = sparePartRepository.findBySn(request.getSn());

        // 3. 存在则更新，不存在则创建
        Spare_part sparePart = existing.orElseGet(Spare_part::new);

        // 4. 设置/更新字段（示例字段映射）
        sparePart.setPartName(purchaseOrder.getSpare_part_name());
        sparePart.setPartModel(purchaseOrder.getSpare_part_model());
        sparePart.setSn(request.getSn()); // 确保SN号唯一
        sparePart.setCategory(convertCategory(request.getSparePartCategory()));
        sparePart.setSparePartStatus(convertStatus(request.getSparePartStatus()));
        sparePart.setLocationId(request.getLocationId());
        sparePart.setManufacturer(request.getManufacturer());
        sparePart.setUnit(request.getUnit());
        sparePart.setStatus("在库");

        // 5. 保存备件
        sparePartRepository.save(sparePart);
    }

    // 添加枚举转换保护方法
    private Spare_part.Category convertCategory(String category) {
        try {
            return Spare_part.Category.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效备件分类: " + category);
        }
    }

    private Spare_part.SparePartStatus convertStatus(String status) {
        try {
            return Spare_part.SparePartStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效备件状态: " + status);
        }
    }

    // 自定义异常类作为静态内部类
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
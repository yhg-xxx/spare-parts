package com.example.service;

import com.example.dao.InboundRecordRepository;
import com.example.dao.InventoryRepository;
import com.example.dao.Purchase_orderRepository;
import com.example.dao.WarehouseRepository;
import com.example.dto.InboundCreateRequest;
import com.example.dto.InboundWithPurchaseDTO;
import com.example.entity.InboundRecord;
import com.example.entity.Inventory;
import com.example.entity.Purchase_order;
import com.example.entity.Warehouse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class InboundRecordService {
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final InboundRecordRepository repository;
    private final Purchase_orderRepository purchaseOrderRepository; // 新增
    public InboundRecordService(InboundRecordRepository repository,
    Purchase_orderRepository purchaseOrderRepository,InventoryRepository inventoryRepository,
                                WarehouseRepository warehouseRepository) {
        this.repository = repository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
    }
    public InboundWithPurchaseDTO findWithPurchaseOrderById(Integer inboundId) {
        InboundRecord inbound = repository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("入库记录不存在"));

        Purchase_order order = purchaseOrderRepository
                .findById(inbound.getOrderId())
                .orElse(null);

        return new InboundWithPurchaseDTO(inbound, order);
    }

    // 新增方法：获取所有入库记录及关联采购单
    // InboundRecordService.java
    public List<InboundWithPurchaseDTO> findAllWithPurchaseOrders(String sparePartName) {
        List<InboundRecord> inbounds = repository.findAll();

        return inbounds.stream()
                .filter(inbound -> {
                    // 获取关联的采购单
                    Purchase_order order = purchaseOrderRepository
                            .findById(inbound.getOrderId())
                            .orElse(null);

                    // 过滤逻辑
                    if (sparePartName == null || sparePartName.isEmpty()) {
                        return true;
                    }
                    return order != null && order.getSpare_part_name() != null
                            && order.getSpare_part_name().contains(sparePartName);
                })
                .map(inbound -> new InboundWithPurchaseDTO(
                        inbound,
                        purchaseOrderRepository.findById(inbound.getOrderId()).orElse(null)
                ))
                .collect(Collectors.toList());
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
        po.setCompleted_at(LocalDateTime.now().toString()); // 根据数据库实际类型调整格式
        purchaseOrderRepository.save(po);
        updateInventory(po, savedRecords.get(0).getLocationId(), purchaseQty);
        return savedRecords;
    }
    private void updateInventory(Purchase_order purchaseOrder, Integer locationId, int inboundQuantity) {
        // 获取仓库名称
        Warehouse warehouse = warehouseRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("仓库信息不存在: " + locationId));

        // 获取备件名称
        String partName = purchaseOrder.getSpare_part_name();
        String locationName = warehouse.getLocation_name();

        // 查询库存记录
        Inventory inventory = inventoryRepository.findByPartNameAndLocationName(partName, locationName);


        // 更新数量（使用实际入库数量）
        int currentQuantity = Integer.parseInt(inventory.getNumber());
        inventory.setNumber(String.valueOf(currentQuantity + inboundQuantity)); // 关键修改点
        inventoryRepository.save(inventory);
    }


    // 自定义异常类作为静态内部类
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
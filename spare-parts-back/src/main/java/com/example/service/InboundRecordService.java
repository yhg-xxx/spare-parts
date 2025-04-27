package com.example.service;

import com.example.config.BusinessException;
import com.example.dao.*;
import com.example.dto.InboundCreateRequest;
import com.example.dto.InboundWithPurchaseDTO;
import com.example.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * 入库记录服务类，负责处理入库记录的创建、查询、删除及Excel导入等业务逻辑
 */
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
    /**
     * 根据入库ID查询入库记录及关联的采购订单
     * @param inboundId 入库记录ID
     * @return 包含入库记录和采购订单的DTO
     * @throws ResourceNotFoundException 当入库记录不存在时抛出
     */
    public InboundWithPurchaseDTO findWithPurchaseOrderById(Integer inboundId) {
        InboundRecord inbound = repository.findById(inboundId)
                .orElseThrow(() -> new ResourceNotFoundException("入库记录不存在"));

        Purchase_order order = purchaseOrderRepository
                .findById(inbound.getOrderId())
                .orElse(null);

        return new InboundWithPurchaseDTO(inbound, order);
    }
    /**
     * 分页查询入库记录（带采购订单信息）
     * @param sparePartName 备件名称筛选条件
     * @param sn 序列号筛选条件
     * @param startTime 起始时间筛选条件（格式：yyyy-MM-dd）
     * @param endTime 结束时间筛选条件（格式：yyyy-MM-dd）
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @return 分页结果集
     */
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

    /**
     * 删除入库记录
     * @param inboundId 入库记录ID
     * @throws ResourceNotFoundException 当记录不存在时抛出
     */
    @Transactional
    public void delete(Integer inboundId) {  // 参数名更明确
        if (!repository.existsById(inboundId)) {
            throw new ResourceNotFoundException("入库记录不存在");
        }
        repository.deleteById(inboundId);
    }
    /**
     * 创建入库记录（批量）
     * @param orderId 关联的采购订单ID
     * @param requests 入库创建请求列表
     * @return 已保存的入库记录列表
     * @throws ResourceNotFoundException 当采购订单不存在时抛出
     * @throws IllegalArgumentException 当数量不匹配或数据格式错误时抛出
     */
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

        Map<Integer, Purchase_order> orderCache = new HashMap<>();
        orderCache.put(orderId, po); // 加入缓存

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
        updateOrderStatus(orderCache); // 统一调用状态更新
        return savedRecords;
    }

    /**
     * 同步备件库信息
     * @param purchaseOrder 关联的采购订单
     * @param request 入库请求数据
     * @throws IllegalArgumentException 当SN号为空时抛出
     */
    private void syncSparePart(Purchase_order purchaseOrder, InboundCreateRequest request) {
        syncSparePart(purchaseOrder,
                request.getSn(),
                request.getSparePartCategory(),
                request.getSparePartStatus(),
                request.getLocationId(),
                request.getManufacturer(),
                request.getUnit());
    }

    /**
     * 同步备件信息（基于字段参数）
     */
    private void syncSparePart(Purchase_order purchaseOrder,
                               String sn,
                               String category,
                               String status,
                               Integer locationId,
                               String manufacturer,
                               String unit) {
        // 原有syncSparePart内容调整为此处实现
        if (sn == null || sn.isBlank()) {
            throw new IllegalArgumentException("SN号不能为空");
        }

        Optional<Spare_part> existing = sparePartRepository.findBySn(sn);
        Spare_part sparePart = existing.orElseGet(Spare_part::new);
        sparePart.setSparePartType(Spare_part.SparePartType.valueOf("正常件"));
        sparePart.setPartName(purchaseOrder.getSpare_part_name());
        sparePart.setPartModel(purchaseOrder.getSpare_part_model());
        sparePart.setSn(sn);
        sparePart.setCategory(convertCategory(category));
        sparePart.setSparePartStatus(convertStatus(status));
        sparePart.setLocationId(locationId);
        sparePart.setManufacturer(manufacturer);
        sparePart.setUnit(unit);
        sparePart.setStatus("在库");

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
    /**
     * 从Excel文件导入入库记录
     * @param file Excel文件
     * @return 成功导入的记录数
     * @throws IOException 文件读取异常
     * @throws RuntimeException 数据校验失败时抛出
     */
// 修正后的列索引定义
    private static final int COL_ORDER_ID = 0;     // A列 采购订单ID
    private static final int COL_LOCATION_ID = 1;  // B列 仓库ID
    private static final int COL_CATEGORY = 2;     // C列 备件分类
    private static final int COL_STATUS = 3;       // D列 备件状态
    private static final int COL_UNIT_PRICE = 4;   // E列 单价
    private static final int COL_TAX_RATE = 5;     // F列 税率
    private static final int COL_SN = 6;           // G列 序列号
    private static final int COL_UNIT = 7;         // H列 单位
    private static final int COL_MANUFACTURER = 8; // I列 生产厂家
    private static final int COL_WARRANTY = 9;     // J列 保修期至
    public int importFromExcel(MultipartFile file) throws IOException {
        List<InboundRecord> records = new ArrayList<>();
        Set<String> snCache = new HashSet<>(); // 新增SN缓存
        Map<Integer, Purchase_order> orderCache = new HashMap<>(); // 采购订单缓存

        try (InputStream is = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            validateHeader(sheet.getRow(0)); // 添加表头校验
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳过表头

                // 辅助方法改为lambda表达式
                Function<Integer, Cell> getCell = cellIndex ->
                        row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                /*---------------- 数据解析逻辑 ----------------*/
                // 1. 基础字段
                int orderId = parseNumericCell(getCell.apply(COL_ORDER_ID), "采购订单ID", row);
                int locationId = parseNumericCell(getCell.apply(COL_LOCATION_ID), "仓库ID", row);

                // 2. 分类与状态（使用中文值转换）
                String category = parseStringCell(getCell.apply(COL_CATEGORY), "备件分类", row);
                String status = parseStringCell(getCell.apply(COL_STATUS), "备件状态", row);
                // 3. 财务字段
                BigDecimal unitPrice = parseDecimalCell(getCell.apply(COL_UNIT_PRICE), "单价", row)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal taxRate = parseDecimalCell(getCell.apply(COL_TAX_RATE), "税率", row)
                        .setScale(4, RoundingMode.HALF_UP);

                // 4. 关键字段（单位/厂家/SN）
                String unit = parseStringCell(getCell.apply(COL_UNIT), "单位", row);
                String manufacturer = parseStringCell(getCell.apply(COL_MANUFACTURER), "生产厂家", row);
                String sn = parseSnCell(getCell.apply(COL_SN), row);

                // 5. 日期处理
                LocalDate warrantyUntil = parseDateCell(getCell.apply(COL_WARRANTY), row);
                /*---------------- 业务校验逻辑 ----------------*/
                // 校验采购订单
                Purchase_order po = validatePurchaseOrder(orderId, row);
                // 校验SN号格式及重复
                validateSnFormat(sn, row);
                if (!snCache.add(sn)) {
                    throw new RuntimeException("第"+(row.getRowNum()+1)+"行SN号重复: "+sn);
                }

                /*---------------- 财务计算逻辑 ----------------*/
                int purchaseQty = Integer.parseInt(po.getNumber().trim());
                BigDecimal quantity = BigDecimal.valueOf(purchaseQty);

                BigDecimal taxFreeUnitPrice = unitPrice.divide(
                        BigDecimal.ONE.add(taxRate), 4, RoundingMode.HALF_UP
                ).setScale(2, RoundingMode.HALF_UP);

                BigDecimal taxPerUnit = unitPrice.subtract(taxFreeUnitPrice);

                syncSparePart(po,
                        sn,
                        category,
                        status,
                        locationId,
                        manufacturer,
                        unit); // 调用字段版本
                // 5. 构建入库记录
                InboundRecord record = new InboundRecord();
                record.setOrderId(orderId);
                record.setLocationId(locationId);
                record.setSn(sn);
                record.setSparePartCategory(parseEnum(category, InboundRecord.SparePartCategory.class));
                record.setSparePartStatus(parseEnum(status, InboundRecord.SparePartStatus.class));
                record.setUnit(unit);
                record.setManufacturer(manufacturer);
                record.setWarrantyUntil(warrantyUntil);
                record.setUnitPrice(unitPrice);
                record.setTaxRate(taxRate);
                record.setTaxFreeUnitPrice(taxFreeUnitPrice);
                record.setTaxAmount(taxPerUnit);
                record.setTaxFreeTotal(taxFreeUnitPrice.multiply(quantity));
                record.setTotalTax(taxPerUnit.multiply(quantity));

                orderCache.put(orderId, po); // 加入缓存
                records.add(record);
            }

            // 6. 批量保存并更新订单状态（与表单逻辑一致）
            List<InboundRecord> savedRecords = repository.saveAll(records);
            updateOrderStatus(orderCache); // 统一调用状态更新
            return savedRecords.size();

        }
    }


    // 通用枚举解析方法
    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumType) {
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的枚举值: " + value + " for " + enumType.getSimpleName());
        }
    }

    // 自定义异常类作为静态内部类
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
    // 表头校验
    private void validateHeader(Row headerRow) {
        String[] expectedHeaders = {
                "采购订单ID",   // 0
                "仓库ID",       // 1
                "备件分类",     // 2
                "备件状态",     // 3
                "单价",         // 4
                "税率",         // 5
                "序列号",       // 6
                "单位",         // 7（第8列）
                "生产厂家",     // 8（第9列）
                "保修期至"      // 9（第10列）
        };
        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !expectedHeaders[i].equals(cell.getStringCellValue().trim())) {
                throw new RuntimeException("表头第"+(i+1)+"列应为'"+expectedHeaders[i]+"'");
            }
        }
    }

    // SN号校验方法修改
    private String parseSnCell(Cell cell, Row row) {
        String sn = parseStringCell(cell, "序列号", row);
        // 修正正则表达式
        String pattern = "^SN\\d{8}-PO\\d+-\\d{3}$";

        if (!sn.matches(pattern)) {
            throw new RuntimeException("第" + (row.getRowNum()+1) + "行SN格式错误\n要求格式示例：SN20240515-PO21-001");
        }
        return sn;
    }

    // 增强型单元格解析
    private int parseNumericCell(Cell cell, String field, Row row) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new RuntimeException(missingCellMsg(field, row));
        }
        try {
            return (int) cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            throw new RuntimeException(typeErrorMsg(field, "数字", row));
        }
    }

    private String parseStringCell(Cell cell, String field, Row row) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new RuntimeException(missingCellMsg(field, row));
        }
        return cell.getStringCellValue().trim();
    }

    private BigDecimal parseDecimalCell(Cell cell, String field, Row row) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new RuntimeException(missingCellMsg(field, row));
        }
        try {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } catch (IllegalStateException e) {
            throw new RuntimeException(typeErrorMsg(field, "数字", row));
        }
    }

    // 错误信息模板
    private String missingCellMsg(String field, Row row) {
        return String.format("第%d行缺失'%s'", row.getRowNum()+1, field);
    }

    private String typeErrorMsg(String field, String type, Row row) {
        return String.format("第%d行'%s'必须为%s类型", row.getRowNum()+1, field, type);
    }

    // 更新订单状态
    private void updateOrderStatus(Map<Integer, Purchase_order> orderCache) {
        // 去重处理
        Set<Purchase_order> uniqueOrders = new HashSet<>(orderCache.values());

        uniqueOrders.forEach(po -> {
            po.setStatus("已入库");
//            po.setCompletedAt(LocalDateTime.now()); // 添加完成时间
            purchaseOrderRepository.save(po);
        });
    }

    // 日期解析（兼容Excel数值和文本格式）
    private LocalDate parseDateCell(Cell cell, Row row) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else {
                String dateStr = cell.getStringCellValue().trim()
                        .replaceAll("/", "-"); // 统一分隔符
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            }
        } catch (Exception e) {
            throw new RuntimeException("第"+(row.getRowNum()+1)+"行日期格式错误，应为yyyy-MM-dd");
        }
    }

    // 采购订单校验
    private Purchase_order validatePurchaseOrder(int orderId, Row row) {
        Purchase_order po = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("第"+(row.getRowNum()+1)+"行采购订单不存在: "+orderId));

        if (!"已完成".equals(po.getStatus())) {
            throw new RuntimeException("订单"+orderId+"状态必须为'已完成'，当前状态: "+po.getStatus());
        }
        return po;
    }
    private void validateSnFormat(String sn, Row row) {
        String[] snParts = sn.split("-");
        if (snParts.length != 3) {
            throw new RuntimeException("第" + (row.getRowNum() + 1) + "行SN格式错误，要求格式示例：SN20240515-PO21-001");
        }

        // 校验序号部分是否为三位数字
        String sequencePart = snParts[2];
        if (!sequencePart.matches("\\d{3}")) {
            throw new RuntimeException("第" + (row.getRowNum() + 1) + "行SN序号必须为三位数字");
        }
    }

   
}
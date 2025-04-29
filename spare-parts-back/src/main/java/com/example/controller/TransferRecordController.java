package com.example.controller;

import com.example.dao.Spare_partRepository;
import com.example.dao.WarehouseRepository;
import com.example.entity.User;
import com.example.service.TransferRecordService;
import com.example.entity.TransferRecord;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transfer")
public class TransferRecordController {

    @Autowired
    private TransferRecordService transferRecordService;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private Spare_partRepository sparePartRepository;

    @GetMapping
    public List<TransferRecord> getAllTransfers() {
        return transferRecordService.getAllTransfers();
    }

    @GetMapping("/{id}")
    public TransferRecord getTransferById(@PathVariable int id) {
        return transferRecordService.getTransferById(id);
    }

    @PostMapping
    public TransferRecord createTransfer(@RequestBody TransferRecord transfer) {
        return transferRecordService.createTransfer(transfer);
    }

    @PostMapping("/batch")
    public List<TransferRecord> createBatchTransfers(@RequestBody List<TransferRecord> transfers) {
        return transferRecordService.createBatchTransfers(transfers);
    }

    @PutMapping("/{transferId}/approve")
    public TransferRecord approveTransfer(@PathVariable int transferId) {
        return transferRecordService.updateStatus(transferId, "已通过");
    }

    @PutMapping("/{transferId}/reject")
    public TransferRecord rejectTransfer(@PathVariable int transferId) {
        return transferRecordService.updateStatus(transferId, "已驳回");
    }
    // TransferRecordController.java
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importTransfers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("applicantId") Integer applicantId
    ) {

        Map<String, Object> result = new HashMap<>();
        List<TransferRecord> successRecords = new ArrayList<>();
        List<Map<String, String>> errorList = new ArrayList<>();
        // 新增参数校验
        if (applicantId == null || applicantId <= 0) {
            result.put("error", "无效的申请人ID");
            return ResponseEntity.badRequest().body(result);
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 遍历Excel行数据
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳过标题行

                try {
                    TransferRecord record = new TransferRecord();
                    record.setFromLocationName(getCellString(row, 0));
                    record.setToLocationName(getCellString(row, 1));
                    record.setPartName(getCellString(row, 2));
                    record.setSn(getCellString(row, 3));
                    record.setTransferReason(getCellString(row, 4));
                    record.setApplicantId(applicantId);
                    record.setStatus("待审核");

                    // 数据校验
                    validateWarehouse(record.getFromLocationName()); // 校验原仓库是否存在
                    validateWarehouse(record.getToLocationName());   // 校验目标仓库是否存在
                    validatePart(record.getPartName(), record.getSn()); // 校验备件有效性

                    successRecords.add(record);
                } catch (Exception e) {
                    errorList.add(new HashMap<>() {{
                        put("row", String.valueOf(row.getRowNum() + 1));
                        put("message", e.getMessage());
                    }});
                }
            }

            // 批量保存有效数据
            if (!successRecords.isEmpty()) {
                transferRecordService.createBatchTransfers(successRecords);
            }

            // 返回结果
            result.put("successCount", successRecords.size());
            result.put("errorCount", errorList.size());
            result.put("errors", errorList);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("error", "文件处理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    // 辅助方法：获取单元格内容
    private String getCellString(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        return cell != null ? cell.getStringCellValue().trim() : "";
    }

    // 校验仓库存在性
    private void validateWarehouse(String name) throws Exception {
        if (!warehouseRepository.existsByLocationName(name))  {
            throw new Exception("仓库不存在: " + name);
        }
    }

    // 校验备件有效性
    private void validatePart(String partName, String sn) throws Exception {
        if (!sparePartRepository.existsByPartNameAndSn(partName, sn)) {
            throw new Exception("备件不存在: " + partName + " - " + sn);
        }
    }
}
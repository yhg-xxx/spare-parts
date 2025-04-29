package com.example.controller;

import com.example.dto.ReturnFactoryDTO;
import com.example.entity.ReturnFactoryRecord;
import com.example.service.ReturnFactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/return-factory")
public class ReturnFactoryController {

    @Autowired
    private ReturnFactoryService returnFactoryService;

    @PostMapping
    public ResponseEntity<ReturnFactoryRecord> createReturnRecord(@RequestBody ReturnFactoryRecord record) {
        return ResponseEntity.ok(returnFactoryService.createReturnRecord(record));
    }

    @PutMapping("/{returnId}/logistics")
    public ResponseEntity<ReturnFactoryRecord> updateLogisticsInfo(
            @PathVariable Integer returnId,
            @RequestParam String logisticsNumber,
            @RequestParam String logisticsCompany,
            @RequestParam String sentTime) {
        try {
            Date parsedDate = parseDate(sentTime);
            return ResponseEntity.ok(returnFactoryService.updateLogisticsInfo(
                    returnId, logisticsNumber, logisticsCompany, parsedDate));
        } catch (ParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{returnId}/result")
    public ResponseEntity<ReturnFactoryRecord> processReturnResult(
            @PathVariable Integer returnId,
            @RequestParam String repairResult,
            @RequestParam String repairDescription,
            @RequestParam String actualReturnTime) {
        try {
            Date parsedDate = parseDate(actualReturnTime);
            return ResponseEntity.ok(returnFactoryService.processReturnResult(
                    returnId, repairResult, repairDescription, parsedDate));
        } catch (ParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{returnId}/confirm")
    public ResponseEntity<ReturnFactoryRecord> confirmRepair(
            @PathVariable Integer returnId,
            @RequestParam String userId) {  // 改为String接收
        try {
            Integer userIdInt = Integer.parseInt(userId);
            return ResponseEntity.ok(returnFactoryService.confirmRepair(returnId, userIdInt));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{returnId}/scrap")
    public ResponseEntity<ReturnFactoryRecord> scrapPart(
            @PathVariable Integer returnId,
            @RequestBody Map<String, Object> requestBody) {

        Integer userId = (Integer) requestBody.get("userId");
        return ResponseEntity.ok(returnFactoryService.scrapPart(returnId, userId));
    }

    @GetMapping
    public ResponseEntity<Page<ReturnFactoryDTO>> searchReturnRecords(
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(returnFactoryService.searchReturnRecords(sn, partName, status, pageable));
    }

    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnFactoryDTO> getReturnRecord(@PathVariable Integer returnId) {
        return ResponseEntity.ok(returnFactoryService.getReturnRecordDetails(returnId));
    }

    private Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return dateFormat.parse(dateStr);
    }
}
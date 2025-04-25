package com.example.controller;

import com.example.service.TransferRecordService;
import com.example.entity.TransferRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer")
public class TransferRecordController {

    @Autowired
    private TransferRecordService transferRecordService;

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
}
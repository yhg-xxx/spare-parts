package com.example.service;

import com.example.dao.TransferRecordRepository;
import com.example.dao.InventoryRepository;
import com.example.entity.TransferRecord;
import com.example.entity.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TransferRecordService {

    @Autowired
    private TransferRecordRepository transferRecordRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<TransferRecord> getAllTransfers() {
        return transferRecordRepository.findAll();
    }

    public TransferRecord getTransferById(int id) {
        return transferRecordRepository.findById(id).orElse(null);
    }

    public TransferRecord createTransfer(TransferRecord transfer) {
        transfer.setStatus("待审核");
        transfer.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return transferRecordRepository.save(transfer);
    }

    public List<TransferRecord> createBatchTransfers(List<TransferRecord> transfers) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        for (TransferRecord transfer : transfers) {
            transfer.setStatus("待审核");
            transfer.setCreatedAt(currentTime);
        }
        return transferRecordRepository.saveAll(transfers);
    }

    @Transactional
    public TransferRecord updateStatus(int id, String status) {
        Optional<TransferRecord> optionalTransfer = transferRecordRepository.findById(id);
        if (optionalTransfer.isPresent()) {
            TransferRecord transfer = optionalTransfer.get();
            transfer.setStatus(status);

            // 只有当状态变为"已通过"时才更新库存
            if ("已通过".equals(status)) {
                updateInventoryForTransfer(transfer);
            }

            return transferRecordRepository.save(transfer);
        }
        return null;
    }

    private void updateInventoryForTransfer(TransferRecord transfer) {
        // 减少原仓库库存
        Inventory fromInventory = inventoryRepository.findByPartNameAndLocationName(
                transfer.getPartName(),
                transfer.getFromLocationName()
        );

        if (fromInventory != null) {
            int currentQuantity = Integer.parseInt(fromInventory.getNumber());






        }
    }
}
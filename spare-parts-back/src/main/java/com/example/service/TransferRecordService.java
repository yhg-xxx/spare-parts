package com.example.service;

import com.example.dao.Spare_partRepository;
import com.example.dao.TransferRecordRepository;
import com.example.dao.InventoryRepository;
import com.example.dao.WarehouseRepository;
import com.example.entity.Spare_part;
import com.example.entity.TransferRecord;
import com.example.entity.Inventory;
import com.example.entity.Warehouse;
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
    @Autowired
    private Spare_partRepository spare_partRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

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
    public TransferRecord updateStatus(int transferId, String status) {
        Optional<TransferRecord> optionalTransfer = transferRecordRepository.findById(transferId);
        if (optionalTransfer.isPresent()) {
            TransferRecord transfer = optionalTransfer.get();
            transfer.setStatus(status);

            // 只有当状态变为"已通过"时才更新库存
            if ("已通过".equals(status)) {
                updateInventoryAndSparePartForTransfer(transfer);
            }

            return transferRecordRepository.save(transfer);
        }
        return null;
    }

    private void updateInventoryAndSparePartForTransfer(TransferRecord transfer) {


        // 3. 更新备件信息
        Optional<Spare_part> sparePartOpt = spare_partRepository.findBySn(transfer.getSn());
        if (sparePartOpt.isPresent()) {
            Spare_part sparePart = sparePartOpt.get();


            // 获取目标仓库的locationId
            Optional<Warehouse> warehouseOpt = warehouseRepository.findByLocationName(transfer.getToLocationName());
            if (warehouseOpt.isPresent()) {
                sparePart.setLocationId(warehouseOpt.get().getLocation_id());
                spare_partRepository.save(sparePart);
            }
        }
    }
}
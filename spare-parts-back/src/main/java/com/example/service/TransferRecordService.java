package com.example.service;

import com.example.dao.TransferRecordRepository;
import com.example.entity.TransferRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TransferRecordService {

    @Autowired
    private TransferRecordRepository transferRecordRepository;

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
    public TransferRecord updateStatus(int id, String status) {
        Optional<TransferRecord> optionalTransfer = transferRecordRepository.findById(id);
        if (optionalTransfer.isPresent()) {
            TransferRecord transfer = optionalTransfer.get();
            transfer.setStatus(status);
            return transferRecordRepository.save(transfer);
        }
        return null;
    }




}
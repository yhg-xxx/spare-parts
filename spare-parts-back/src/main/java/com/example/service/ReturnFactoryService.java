package com.example.service;

import com.example.dao.ReturnFactoryRepository;
import com.example.dao.Spare_partRepository;
import com.example.dto.ReturnFactoryDTO;
import com.example.entity.ReturnFactoryRecord;
import com.example.entity.Spare_part;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ReturnFactoryService {

    @Autowired
    private ReturnFactoryRepository returnFactoryRepository;

    @Autowired
    private Spare_partRepository sparePartRepository;

    @Transactional
    public ReturnFactoryRecord createReturnRecord(ReturnFactoryRecord record) {
        Spare_part sparePart = sparePartRepository.findById(record.getPartId())
                .orElseThrow(() -> new RuntimeException("备件不存在"));

        if (!"坏件".equals(sparePart.getSparePartStatus().name()) &&
                !"返厂修".equals(sparePart.getSparePartStatus().name())) {
            throw new RuntimeException("备件状态不符合返厂要求");
        }

        record.setStatus(ReturnFactoryRecord.ReturnStatus.待返厂);
        record.setCreatedAt(new Date());

        sparePart.setSparePartStatus(Spare_part.SparePartStatus.返厂修);
        sparePartRepository.save(sparePart);

        return returnFactoryRepository.save(record);
    }

    @Transactional
    public ReturnFactoryRecord updateLogisticsInfo(Integer returnId, String logisticsNumber,
                                                   String logisticsCompany, Date sentTime) {
        ReturnFactoryRecord record = returnFactoryRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("返厂记录不存在"));

        if (!ReturnFactoryRecord.ReturnStatus.待返厂.equals(record.getStatus())) {
            throw new RuntimeException("只有待返厂状态的记录可以更新物流信息");
        }

        record.setLogisticsNumber(logisticsNumber);
        record.setLogisticsCompany(logisticsCompany);
        record.setSentTime(sentTime);
        record.setStatus(ReturnFactoryRecord.ReturnStatus.已返厂);

        long expectedReturnTime = sentTime.getTime() + record.getExpectedRepairDays() * 24 * 60 * 60 * 1000L;
        record.setExpectedReturnTime(new Date(expectedReturnTime));

        return returnFactoryRepository.save(record);
    }

    @Transactional
    public ReturnFactoryRecord processReturnResult(Integer returnId, String repairResult,
                                                   String repairDescription, Date actualReturnTime) {
        ReturnFactoryRecord record = returnFactoryRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("返厂记录不存在"));

        if (!ReturnFactoryRecord.ReturnStatus.已返厂.equals(record.getStatus()) &&
                !ReturnFactoryRecord.ReturnStatus.维修中.equals(record.getStatus())) {
            throw new RuntimeException("只有已返厂或维修中状态的记录可以处理返回结果");
        }

        record.setRepairResult(ReturnFactoryRecord.RepairResult.valueOf(repairResult));
        record.setRepairDescription(repairDescription);
        record.setActualReturnTime(actualReturnTime);
        record.setStatus(ReturnFactoryRecord.ReturnStatus.已返回);

        Spare_part sparePart = sparePartRepository.findById(record.getPartId())
                .orElseThrow(() -> new RuntimeException("备件不存在"));

        if ("修复成功".equals(repairResult)) {
            sparePart.setSparePartStatus(Spare_part.SparePartStatus.修好件);
        } else {
            sparePart.setSparePartStatus(Spare_part.SparePartStatus.坏件);
        }

        sparePartRepository.save(sparePart);

        return returnFactoryRepository.save(record);
    }

    @Transactional
    public ReturnFactoryRecord confirmRepair(Integer returnId, Integer userId) {
        ReturnFactoryRecord record = returnFactoryRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("返厂记录不存在"));

        if (!ReturnFactoryRecord.ReturnStatus.已返回.equals(record.getStatus())) {
            throw new RuntimeException("只有已返回状态的记录可以确认验收");
        }

        if (!ReturnFactoryRecord.RepairResult.修复成功.equals(record.getRepairResult())) {
            throw new RuntimeException("只有修复成功的备件可以验收");
        }

        record.setStatus(ReturnFactoryRecord.ReturnStatus.已验收);
        return returnFactoryRepository.save(record);
    }

    @Transactional
    public ReturnFactoryRecord scrapPart(Integer returnId, Integer userId) {
        ReturnFactoryRecord record = returnFactoryRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("返厂记录不存在"));

        if (!ReturnFactoryRecord.ReturnStatus.已返回.equals(record.getStatus()) ||
                !ReturnFactoryRecord.RepairResult.修复失败.equals(record.getRepairResult())) {
            throw new RuntimeException("只有修复失败的备件可以报废");
        }

        Spare_part sparePart = sparePartRepository.findById(record.getPartId())
                .orElseThrow(() -> new RuntimeException("备件不存在"));

        sparePart.setSparePartStatus(Spare_part.SparePartStatus.已报废);
        sparePartRepository.save(sparePart);

        record.setStatus(ReturnFactoryRecord.ReturnStatus.已报废);
        return returnFactoryRepository.save(record);
    }

    public Page<ReturnFactoryDTO> searchReturnRecords(String sn, String partName, String status, Pageable pageable) {
        return returnFactoryRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(sn)) {
                predicates.add(cb.like(root.get("sn"), "%" + sn + "%"));
            }

            if (StringUtils.hasText(partName)) {
                Join<ReturnFactoryRecord, Spare_part> partJoin = root.join("part", JoinType.INNER);
                predicates.add(cb.like(partJoin.get("partName"), "%" + partName + "%"));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), ReturnFactoryRecord.ReturnStatus.valueOf(status)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(this::convertToDTO);
    }

    public ReturnFactoryDTO getReturnRecordDetails(Integer returnId) {
        ReturnFactoryRecord record = returnFactoryRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("返厂记录不存在"));
        return convertToDTO(record);
    }

    private ReturnFactoryDTO convertToDTO(ReturnFactoryRecord record) {
        ReturnFactoryDTO dto = new ReturnFactoryDTO();
        BeanUtils.copyProperties(record, dto);

        // 显式处理枚举字段
        if(record.getStatus() != null) {
            dto.setStatus(record.getStatus().name());
        }
        if(record.getRepairResult() != null) {
            dto.setRepairResult(record.getRepairResult().name());
        }

        Spare_part part = sparePartRepository.findById(record.getPartId())
                .orElseThrow(() -> new RuntimeException("备件不存在"));

        dto.setPartName(part.getPartName());
        dto.setPartModel(part.getPartModel());
        dto.setManufacturerName(part.getManufacturer());

        return dto;
    }
}
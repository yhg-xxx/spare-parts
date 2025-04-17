package com.example.service;

import com.example.config.ResourceNotFoundException;
import com.example.dao.UsageRequestRepository;
import com.example.entity.UsageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class UsageRequestService {

    @Autowired
    private UsageRequestRepository usageRequestRepository;

    public UsageRequest createUsageRequest(UsageRequest usageRequest) {
        // 设置初始状态为"待审核"
        usageRequest.setStatus("待审核");

        // 设置创建时间（使用中国时区）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        usageRequest.setCreateTime(sdf.format(new Date()));

        return usageRequestRepository.save(usageRequest);
    }

    public List<UsageRequest> getAllUsageRequests() {
        return usageRequestRepository.findAll();
    }

    public UsageRequest getUsageRequestById(Integer id) {
        return usageRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("申请单不存在"));
    }

    public void approveUsageRequest(Integer id) {
        UsageRequest request = getUsageRequestById(id);

        // 状态校验
        if (!"待审核".equals(request.getStatus())) {
            throw new IllegalStateException("当前状态不允许审批");
        }

        // 根据类型设置不同状态
        if (request.getType() == UsageRequest.UsageType.维修借用) {
            request.setStatus("待出库");
        } else {
            request.setStatus("已批准");
        }

        usageRequestRepository.save(request);
    }

    public void rejectUsageRequest(Integer id) {
        UsageRequest request = getUsageRequestById(id);

        if (!"待审核".equals(request.getStatus())) {
            throw new IllegalStateException("当前状态不允许审批");
        }

        request.setStatus("已拒绝");
        usageRequestRepository.save(request);
    }
    /*// UsageRequestService.java 添加新方法
    public List<UsageRequest> getByApplicantId(Integer applicantId) {
        return usageRequestRepository.findByApplicantId(applicantId);
    }*/
}
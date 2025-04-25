package com.example.service;

import com.example.config.ResourceNotFoundException;
import com.example.dao.UsageRequestRepository;
import com.example.entity.UsageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UsageRequestService {

    @Autowired
    private UsageRequestRepository usageRequestRepository;

    public UsageRequest createUsageRequest(UsageRequest usageRequest) {
        // 新增校验逻辑
        if (StringUtils.isEmpty(usageRequest.getPartName())) {
            throw new IllegalArgumentException("备件名称不能为空");
        }

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
            request.setStatus("待出库");
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
    public void updateUsageRequestStatus(Integer id, String newStatus) {
        UsageRequest request = usageRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("申请单不存在"));

        // 可添加状态校验逻辑（可选）
        Set<String> validStatuses = new HashSet<>(Arrays.asList(
                "待审核", "待出库", "已拒绝", "已出库"
        ));

        if (!validStatuses.contains(newStatus)) {
            throw new IllegalStateException("无效的状态值: " + newStatus);
        }

        request.setStatus(newStatus);
        usageRequestRepository.save(request);
    }
    public void completeRequest(Integer id) {
        UsageRequest request = usageRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("领用单不存在"));

        if (!"待出库".equals(request.getStatus())) {
            throw new RuntimeException("当前状态不可完成");
        }

        // 修正类型判断方式（使用枚举比较）
        String newStatus = request.getType() == UsageRequest.UsageType.维修借用
                ? "已出库-维修借用"
                : "已出库-维修申领";

        request.setStatus(newStatus);
        usageRequestRepository.save(request);
    }

}
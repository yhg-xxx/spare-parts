package com.example.service;

import com.example.dao.ScrapRecordRepository;
import com.example.entity.ScrapRecord;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ScrapRecordService {
    private final RestTemplate restTemplate;
    private final ScrapRecordRepository repository;
    // 文件上传路径


    public ScrapRecordService(ScrapRecordRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    /**
     * 保存或更新报废记录（含文件上传）
     */
    public ScrapRecord saveRecord(ScrapRecord record, MultipartFile damagePhoto, String applicantId) throws IOException {
        record.setApplyTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        record.setPartStatus(ScrapRecord.PartStatus.待审核);
        record.setApplicantId(applicantId);

        if (damagePhoto != null && !damagePhoto.isEmpty()) {
            String fileUrl = uploadFileToFileController(damagePhoto);
            record.setDamagePhoto(fileUrl);
        }

        return repository.save(record);
    }

    /**
     * 单独上传照片并更新记录
     */
    public void uploadDamagePhoto(Integer orderId, MultipartFile file) throws IOException {
        ScrapRecord record = repository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        if (file != null && !file.isEmpty()) {
            String fileUrl = uploadFileToFileController(file);
            record.setDamagePhoto(fileUrl);
            repository.save(record);
        }
    }

    /**
     * 获取所有记录
     */
    public List<ScrapRecord> findAll() {
        return repository.findAll();
    }

    /**
     * 生成唯一文件名（UUID + 时间戳）
     */
    private String generateUniqueFilename(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String fileExtension = originalName.substring(originalName.lastIndexOf("."));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return UUID.randomUUID() + "_" + timestamp + fileExtension;
    }
    public List<ScrapRecord> findByApplicantId(String applicantId) {
        return repository.findByApplicantId(applicantId);
    }
    // 新增文件访问方法
    public String getFullImagePath(String filename) {
        return "/uploads/" + filename;  // 与静态资源路径对应
    }
    // 在 ScrapRecordService.java 中添加
// 在类中添加路径校验方法

    private String uploadFileToFileController(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:8080/files/upload",
                requestEntity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && (Integer) responseBody.get("code") == 200) {
                return (String) responseBody.get("url");
            } else {
                throw new RuntimeException("文件上传失败: " + responseBody.get("msg"));
            }
        } else {
            throw new RuntimeException("文件上传失败，状态码: " + response.getStatusCode());
        }
    }
    public ScrapRecord updateReview(Integer orderId, Map<String, Object> updates) {
        ScrapRecord record = repository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        // 强制设置审核时间为当前时间
        record.setScrapTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 更新状态
        if (updates.containsKey("partStatus")) {
            record.setPartStatus(ScrapRecord.PartStatus.valueOf(updates.get("partStatus").toString()));
        }

        // 更新处置方式
        if (updates.containsKey("disposalMethod")) {
            record.setDisposalMethod(updates.get("disposalMethod").toString());
        }

        // 更新执行人
        if (updates.containsKey("executor")) {
            record.setExecutor(updates.get("executor").toString());
        }

        // 更新报废时间
        if (updates.containsKey("scrapTime") && updates.get("scrapTime") != null) {
            record.setScrapTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return repository.save(record);
    }
    public List<ScrapRecord> findByPartStatus(ScrapRecord.PartStatus partStatus) {
        return repository.findByPartStatus(partStatus);
    }
}
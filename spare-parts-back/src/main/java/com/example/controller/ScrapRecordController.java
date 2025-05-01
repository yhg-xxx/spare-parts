package com.example.controller;


import com.example.entity.ScrapRecord;
import com.example.service.ScrapRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/scrapRecord/scrap-records")
public class ScrapRecordController {


    private final ScrapRecordService service;

    public ScrapRecordController(ScrapRecordService service) {
        this.service = service;
    }

    /**
     * 创建记录（带文件上传）
     */
    @PostMapping
    public ResponseEntity<ScrapRecord> createRecord(
            @RequestParam("sn") String sn,
            @RequestParam("scrapReason") String scrapReason,
            @RequestParam("applicantId") String applicantId,
            @RequestParam(value = "damagePhoto", required = false) MultipartFile file
    ) throws IOException {
        ScrapRecord record = new ScrapRecord();
        record.setSn(sn);
        record.setScrapReason(scrapReason);
        // 其他字段由服务层填充

        ScrapRecord savedRecord = service.saveRecord(record, file, applicantId);
        return ResponseEntity.ok(savedRecord);
    }

    /**
     * 单独上传/更新照片
     */
    @PostMapping("/{orderId}/upload-photo")
    public ResponseEntity<String > uploadPhoto(
            @PathVariable Integer orderId,
            @RequestParam("file") MultipartFile file) throws IOException {
        service.uploadDamagePhoto(orderId, file);
        return ResponseEntity.ok("Photo uploaded successfully");
    }
    // 新增查询接口
    @GetMapping("/my-records")
    public ResponseEntity<List<ScrapRecord>> getMyRecords(
            @RequestParam String applicantId
    ) {
        List<ScrapRecord> records = service.findByApplicantId(applicantId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/photo-url")
    public ResponseEntity<String> getPhotoRelativePath(
            @RequestParam String damagePhoto) {

        // 安全校验文件名格式
        if (damagePhoto.contains("..")) {
            return ResponseEntity.badRequest().body("非法文件名");
        }

        // 生成可访问的相对路径（对应静态资源映射）
        String relativePath = damagePhoto;
        return ResponseEntity.ok(relativePath);
    }
    @PutMapping("/{orderId}/review")
    public ResponseEntity<ScrapRecord> updateReview(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> updates) {
        ScrapRecord updatedRecord = service.updateReview(orderId, updates);
        return ResponseEntity.ok(updatedRecord);
    }
    @GetMapping
    public ResponseEntity<List<ScrapRecord>> getRecordsByStatus(
            @RequestParam(required = false) ScrapRecord.PartStatus partStatus) {
        if (partStatus == null) {
            return ResponseEntity.ok(service.findAll());
        }
        return ResponseEntity.ok(service.findByPartStatus(partStatus));
    }
    /**
     * 全局异常处理（示例）
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleFileUploadError(IOException e) {
        return ResponseEntity.internalServerError().body("文件上传失败: " + e.getMessage());
    }
}
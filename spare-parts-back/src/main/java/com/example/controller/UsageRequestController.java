package com.example.controller;

import com.example.config.ResourceNotFoundException;
import com.example.entity.UsageRequest;
import com.example.service.UsageRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usage-requests")
public class UsageRequestController {

    @Autowired
    private UsageRequestService usageRequestService;

    @PostMapping
    public ResponseEntity<UsageRequest> create(@RequestBody UsageRequest request) {
        return ResponseEntity.ok(usageRequestService.createUsageRequest(request));
    }

    @GetMapping
    public ResponseEntity<List<UsageRequest>> getAll() {
        return ResponseEntity.ok(usageRequestService.getAllUsageRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsageRequest> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(usageRequestService.getUsageRequestById(id));
    }


    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Integer id) {
        usageRequestService.approveUsageRequest(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Integer id) {
        usageRequestService.rejectUsageRequest(id);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> requestBody
    ) {
        String newStatus = requestBody.get("status");
        usageRequestService.updateUsageRequestStatus(id, newStatus);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeRequest(@PathVariable Integer id) {
        usageRequestService.completeRequest(id);
        return ResponseEntity.noContent().build();
    }
    

    // 异常处理
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class})
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}


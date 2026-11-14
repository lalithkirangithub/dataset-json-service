package com.example.dataset.controller;

import com.example.dataset.dto.InsertRecordResponse;
import com.example.dataset.dto.SortedResponse;
import com.example.dataset.service.DatasetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dataset")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    @PostMapping("/{datasetName}/record")
    public ResponseEntity<InsertRecordResponse> insertRecord(
            @PathVariable String datasetName,
            @RequestBody Map<String, Object> record) {

        InsertRecordResponse response = service.insertRecord(datasetName, record);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{datasetName}/query")
    public ResponseEntity<?> queryRecords(
            @PathVariable String datasetName,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {

        boolean hasGroup = groupBy != null && !groupBy.isBlank();
        boolean hasSort = sortBy != null && !sortBy.isBlank();

        if (hasGroup) {
            return ResponseEntity.ok(service.groupRecords(datasetName, groupBy, sortBy, order));
        }
        if (hasSort) {
            return ResponseEntity.ok(new SortedResponse(service.sortRecords(datasetName, sortBy, order)));
        }
        return ResponseEntity.ok(Map.of("records", service.getAllRecords(datasetName)));
    }
}
package com.example.dataset.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dataset.dto.InsertRecordResponse;
import com.example.dataset.dto.SortedResponse;
import com.example.dataset.service.DatasetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dataset")
@Tag(name = "Dataset", description = "Insert JSON records and query them with group-by and sort-by")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    @Operation(summary = "Insert a JSON record into a dataset",
            description = "The body can be any JSON object but must contain an 'id'. "
                    + "Returns 201 on success, 400 if 'id' is missing, 409 if the id already exists.")
    @PostMapping("/{datasetName}/record")
    public ResponseEntity<InsertRecordResponse> insertRecord(
            @Parameter(description = "Dataset name, e.g. employees") @PathVariable String datasetName,
            @RequestBody Map<String, Object> record) {

        InsertRecordResponse response = service.insertRecord(datasetName, record);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Query a dataset with optional group-by and sort-by",
            description = "No parameters returns {records}. groupBy returns {groupedRecords}. "
                    + "sortBy returns {sortedRecords}. If both are given, records are sorted and then grouped. "
                    + "Returns 400 for an invalid order and 404 for an unknown dataset.")
    @GetMapping("/{datasetName}/query")
    public ResponseEntity<?> queryRecords(
            @Parameter(description = "Dataset name, e.g. employees") @PathVariable String datasetName,
            @Parameter(description = "Field to group by, e.g. department") @RequestParam(required = false) String groupBy,
            @Parameter(description = "Field to sort by, e.g. age") @RequestParam(required = false) String sortBy,
            @Parameter(description = "asc (default) or desc") @RequestParam(required = false) String order) {

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
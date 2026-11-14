package com.example.dataset.service;

import com.example.dataset.dto.GroupedResponse;
import com.example.dataset.dto.InsertRecordResponse;
import com.example.dataset.entity.DatasetRecord;
import com.example.dataset.exception.DatasetNotFoundException;
import com.example.dataset.exception.DuplicateRecordException;
import com.example.dataset.repository.DatasetRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DatasetService {

    private final DatasetRepository repository;
    private final ObjectMapper objectMapper;

    public DatasetService(DatasetRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    // ---------- Insert ----------
    public InsertRecordResponse insertRecord(String datasetName, Map<String, Object> record) {
        if (record == null || record.isEmpty() || record.get("id") == null) {
            throw new IllegalArgumentException("Record must be a JSON object containing an 'id' field");
        }

        String recordId = String.valueOf(record.get("id"));

        if (repository.existsByDatasetNameAndRecordId(datasetName, recordId)) {
            throw new DuplicateRecordException(datasetName, recordId);
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(record);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid JSON record");
        }

        repository.save(new DatasetRecord(datasetName, recordId, json));

        return new InsertRecordResponse("Record added successfully", datasetName, recordId);
    }

    // ---------- Query ----------
    public List<Map<String, Object>> getAllRecords(String datasetName) {
        List<DatasetRecord> rows = repository.findByDatasetName(datasetName);

        if (rows.isEmpty()) {
            throw new DatasetNotFoundException(datasetName);
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (DatasetRecord row : rows) {
            Map<String, Object> map = objectMapper.readValue(
                    row.getData(), new TypeReference<Map<String, Object>>() {});
            records.add(map);
        }
        return records;
    }

    public List<Map<String, Object>> sortRecords(String datasetName, String sortBy, String order) {
        return sortList(getAllRecords(datasetName), sortBy, order);
    }

    // Group records. If sortBy is given, records are sorted first, so each group stays sorted.
    public GroupedResponse groupRecords(String datasetName, String groupBy, String sortBy, String order) {
        List<Map<String, Object>> records = getAllRecords(datasetName);

        if (sortBy != null && !sortBy.isBlank()) {
            records = sortList(records, sortBy, order);
        }

        Map<String, List<Map<String, Object>>> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        record -> {
                            Object value = record.get(groupBy);
                            return value == null ? "UNKNOWN" : String.valueOf(value);
                        },
                        LinkedHashMap::new,
                        Collectors.toList()));

        return new GroupedResponse(grouped);
    }

    // ---------- Helpers ----------
    private List<Map<String, Object>> sortList(List<Map<String, Object>> records, String sortBy, String order) {
        boolean descending = isDescending(order);

        Comparator<Object> valueComparator = this::compareValues;
        if (descending) {
            valueComparator = valueComparator.reversed();
        }

        // records missing the field always go to the end
        Comparator<Map<String, Object>> comparator =
                Comparator.comparing(r -> r.get(sortBy), Comparator.nullsLast(valueComparator));

        return records.stream().sorted(comparator).collect(Collectors.toList());
    }

    private boolean isDescending(String order) {
        if (order == null || order.isBlank() || order.equalsIgnoreCase("asc")) {
            return false;
        }
        if (order.equalsIgnoreCase("desc")) {
            return true;
        }
        throw new IllegalArgumentException("Invalid order '" + order + "'. Use 'asc' or 'desc'");
    }

    private int compareValues(Object a, Object b) {
        if (a instanceof Number x && b instanceof Number y) {
            return Double.compare(x.doubleValue(), y.doubleValue());
        }
        return String.valueOf(a).compareToIgnoreCase(String.valueOf(b));
    }
}
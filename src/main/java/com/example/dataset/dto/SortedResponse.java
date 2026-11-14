package com.example.dataset.dto;

import java.util.List;
import java.util.Map;
public record SortedResponse(List<Map<String, Object>> sortedRecords) {}

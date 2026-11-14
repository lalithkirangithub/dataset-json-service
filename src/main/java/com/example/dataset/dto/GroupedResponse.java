package com.example.dataset.dto;

import java.util.List;
import java.util.Map;

public record GroupedResponse(Map<String, List<Map<String, Object>>> groupedRecords){}

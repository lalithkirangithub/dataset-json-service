package com.example.dataset.repository;

import com.example.dataset.entity.DatasetRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetRepository extends JpaRepository<DatasetRecord, Long> {

    List<DatasetRecord> findByDatasetName(String datasetName);

    boolean existsByDatasetNameAndRecordId(String datasetName, String recordId);
}
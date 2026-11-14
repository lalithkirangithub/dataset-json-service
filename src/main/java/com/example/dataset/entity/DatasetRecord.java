package com.example.dataset.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dataset_record")
public class DatasetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_name", nullable = false)
    private String datasetName;

    @Column(name = "record_id")
    private String recordId;

    @Column(name = "data", columnDefinition = "TEXT", nullable = false)
    private String data;

    public DatasetRecord() {}

    public DatasetRecord(String datasetName, String recordId, String data) {
        this.datasetName = datasetName;
        this.recordId = recordId;
        this.data = data;
    }

    public Long getId() { return id; }
    public String getDatasetName() { return datasetName; }
    public String getRecordId() { return recordId; }
    public String getData() { return data; }

    public void setId(Long id) { this.id = id; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public void setData(String data) { this.data = data; }
}
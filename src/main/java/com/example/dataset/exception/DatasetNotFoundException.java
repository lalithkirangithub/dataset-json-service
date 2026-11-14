package com.example.dataset.exception;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(String datasetName) {
        super("Dataset not found: " + datasetName);
    }
}

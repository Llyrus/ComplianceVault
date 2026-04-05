package com.compliancevault.model;

import java.io.File;
import java.time.LocalDate;

public class Document {
    private int fileId;
    private String fileName;
    private String filePath;
    private LocalDate uploadDate;
    private String fileType;


    public Document(int fileId, String fileName, String filePath,
                    LocalDate uploadDate, String fileType) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
        this.fileType = fileType;
    }

    public void open() {
        //TODO: finish..
    }

    public File getFile() {
        return new File(filePath);
    }

}

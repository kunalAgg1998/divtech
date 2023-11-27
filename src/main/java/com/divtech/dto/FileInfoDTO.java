package com.divtech.dto;

import lombok.Data;

import java.util.Date;

/**
 * Data Transfer Object (DTO) representing file information.
 */
@Data
public class FileInfoDTO {
    private String id;            // Unique identifier for the file
    private String fileName;      // Name of the file
    private Date uploadDate;      // Date when the file was uploaded
}

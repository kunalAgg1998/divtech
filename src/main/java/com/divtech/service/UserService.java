package com.divtech.service;

import com.divtech.dto.FileInfoDTO;
import com.divtech.entity.User;
import com.divtech.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * Service class handling user-related operations and file processing.
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // ConcurrentHashMap to store file processing status
    private final ConcurrentHashMap<Long, String> fileProcessingStatus = new ConcurrentHashMap<>();


    /**
     * Process an Excel file containing user data and save it to the database.
     *
     * @param file   The Excel file to be processed.
     * @param fileId The ID associated with the file being processed.
     * @throws IOException If an I/O error occurs while processing the file.
     */
    public void processExcelFile(MultipartFile file, Long fileId) throws IOException {
        fileProcessingStatus.put(fileId, "Processing");
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        while (rows.hasNext()) {
            Row currentRow = rows.next();

            // Skip header
            if (currentRow.getRowNum() == 0) {
                continue;
            }

            // Create a User object and map data from Excel rows
            User user = new User();
            user.setId(getCellValueAsString(currentRow.getCell(0)));
            user.setName(getCellValueAsString(currentRow.getCell(1)));
            user.setAddress(getCellValueAsString(currentRow.getCell(2)));
            user.setPhone(getCellValueAsString(currentRow.getCell(3)));

            // Save user to the database
            userRepository.save(user);
        }

        workbook.close();
        fileProcessingStatus.put(fileId, "Completed");
    }

    // Utility method to get cell value as a string
    private String getCellValueAsString(Cell cell) {
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // Format the date into a string (if needed)
                        return cell.getDateCellValue().toString();
                    } else {
                        // Convert numeric value to string
                        return Double.toString(cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return Boolean.toString(cell.getBooleanCellValue());
                default:
                    return "";
            }
        }
        return "";
    }

    public String getProcessingStatus(Long fileId) {
        // Implement your logic to track and return the file processing status
        return fileProcessingStatus.getOrDefault(fileId, "Unknown File ID");
    }

    public List<FileInfoDTO> getAllUploadedFiles() {
        return userRepository.findAll().stream()
                .map(user -> {
                    FileInfoDTO dto = new FileInfoDTO();
                    dto.setId(user.getId());
                    dto.setFileName(user.getName());
                    dto.setUploadDate(user.getLastAccessTime());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public FileInfoDTO reviewFile(Long fileId, String username) {
//        User file = userRepository.findById(fileId.intValue()).orElseThrow(() -> new RuntimeException("File not found"));
        User file = userRepository.findByName(username).get();
        // Update last access time
        file.setLastAccessTime(new Date());

        // Log the user's information who reviewed the file
        // This could be a simple log statement or a more complex logging mechanism
        logUserAccess(fileId, username);

        userRepository.save(file);

        // Convert and return the file details as DTO
        return convertToDto(file);

    }
    private void logUserAccess(Long fileId, String username) {
        // Implement logging logic here
        log.info("File with ID: " + fileId + " was accessed by user: " + username);
    }

    private FileInfoDTO convertToDto(User file) {
        FileInfoDTO dto = new FileInfoDTO();
        dto.setId(file.getId());
        dto.setFileName(file.getName());
        dto.setUploadDate(file.getLastAccessTime());
        return dto;
    }

    public void deleteFile(String username) {
        // Check if the file exists
        User file = userRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Delete the file
        userRepository.delete(file);
    }
}
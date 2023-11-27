package com.divtech.controller;

import com.divtech.dto.FileInfoDTO;
import com.divtech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Controller handling file-related operations for users.
 */

@RestController
@RequestMapping("/api/files")
public class UserController {

    @Autowired
    private UserService userService;



    /**
     * Endpoint to upload a file and start processing it.
     *
     * @param file The file to be uploaded.
     * @return ResponseEntity indicating the status of the upload process.
     */

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Long fileId = generateFileId(); // Implement this method to generate a unique file ID
            userService.processExcelFile(file, fileId);
            return ResponseEntity.ok("File is being processed with ID: " + fileId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }



    /**
     * Generates a unique file ID (example implementation using system time).
     *
     * @return Unique file ID as a Long.
     */
    private Long generateFileId() {
        // Your logic to generate a unique file ID
        return System.currentTimeMillis(); // Example implementation
    }



    /**
     * Retrieves the processing status of a file based on its ID.
     *
     * @param fileId The ID of the file being processed.
     * @return ResponseEntity containing the processing status of the file.
     */

    @GetMapping("/progress/{fileId}")
    public ResponseEntity<String> checkProgress(@PathVariable Long fileId) {
        String status = userService.getProcessingStatus(fileId);
        return ResponseEntity.ok(status);
    }



    /**
     * Retrieves a list of all uploaded files.
     *
     * @return ResponseEntity containing a list of FileInfoDTO representing uploaded files.
     */

    @GetMapping("/list")
    public ResponseEntity<List<FileInfoDTO>> listAllFiles() {
        List<FileInfoDTO> files = userService.getAllUploadedFiles();
        return ResponseEntity.ok(files);
    }



    /**
     * Retrieves file information for review based on its ID and username.
     *
     * @param fileId   The ID of the file to be reviewed.
     * @param username The username of the reviewer.
     * @return ResponseEntity containing the FileInfoDTO for review.
     */

    @GetMapping("/review/{fileId}")
    public ResponseEntity<FileInfoDTO> reviewFile(@PathVariable Long fileId, @RequestParam String username) {
        FileInfoDTO fileInfo = userService.reviewFile(fileId, username);
        return ResponseEntity.ok(fileInfo);
    }



    /**
     * Deletes a file record based on the username.
     *
     * @param username The username associated with the file record to be deleted.
     * @return ResponseEntity indicating the success of the deletion process.
     */

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<?> deleteFile(@PathVariable String username) {
        userService.deleteFile(username);
        return ResponseEntity.ok("User record deleted successfully");
    }
}

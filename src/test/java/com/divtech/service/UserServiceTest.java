package com.divtech.service;

import com.divtech.dto.FileInfoDTO;
import com.divtech.entity.User;
import com.divtech.repository.UserRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private ConcurrentHashMap<Long, String> mockFileProcessingStatus;

    @InjectMocks
    private UserService userServiceUnderTest;

    private ConcurrentHashMap<Long, String> fileProcessingStatus;

    @BeforeEach
    void setUp() {
        // TODO: Set the following fields: fileProcessingStatus.

    }

    private MultipartFile createMockExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Sheet");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Address");
            header.createCell(3).setCellValue("Phone");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(1);
            dataRow.createCell(1).setCellValue("Test User");
            dataRow.createCell(2).setCellValue("Test Address");
            dataRow.createCell(3).setCellValue("1234567890");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] bytes = bos.toByteArray();
            return new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream(bytes));
        }
    }

    @Test
    public void testProcessExcelFile_Success() throws IOException {
        Long fileId = 1L;
        MultipartFile mockFile = createMockExcelFile();

        userServiceUnderTest.processExcelFile(mockFile, fileId);

        // Verify interactions
        verify(mockUserRepository, times(1)).save(any(User.class)); // Verify that save was called once


    }

    @Test
    public void testProcessExcelFile_IOException() throws IOException {
        Long fileId = 1L;
        MultipartFile mockFile = mock(MultipartFile.class);

        // Simulate IOException
        when(mockFile.getInputStream()).thenThrow(new IOException("Failed to read file"));

        assertThrows(IOException.class, () -> {
            userServiceUnderTest.processExcelFile(mockFile, fileId);
        });

        // Verify that no user is saved in case of IOException
        verify(mockUserRepository, never()).save(any(User.class));
    }




    @Test
    void testGetAllUploadedFiles() {
        // Setup
        final FileInfoDTO fileInfoDTO = new FileInfoDTO();
        fileInfoDTO.setId("1");
        fileInfoDTO.setFileName("abc");
        fileInfoDTO.setUploadDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        final List<FileInfoDTO> expectedResult = Arrays.asList(fileInfoDTO);

        // Configure UserRepository.findAll(...).
        final List<User> users = Arrays.asList(new User("1", "abc", "bca", "586577874878",
                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()));
        when(mockUserRepository.findAll()).thenReturn(users);

        // Run the test
        final List<FileInfoDTO> result = userServiceUnderTest.getAllUploadedFiles();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetAllUploadedFiles_UserRepositoryReturnsNoItems() {
        // Setup
        when(mockUserRepository.findAll()).thenReturn(Collections.emptyList());

        // Run the test
        final List<FileInfoDTO> result = userServiceUnderTest.getAllUploadedFiles();

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }



    // Utility method to create a User object
    private User createUser(Long id) {
        User user = new User();
        user.setId("1");
        user.setName("Test File");
        user.setLastAccessTime(new Date());
        return user;
    }
    private User findUser(String username) {
        User user = new User();
        user.setId("1");
        user.setName("Test File");
        user.setLastAccessTime(new Date());
        return user;
    }
    @Test
    public void testReviewFile_Success() {
        Long fileId = 1L;
        String username = "Test File";

        User mockUser = findUser(username);
        when(mockUserRepository.findByName(username)).thenReturn(Optional.of(mockUser));

        FileInfoDTO fileInfoDTO = userServiceUnderTest.reviewFile(fileId, username);

        assertNotNull(fileInfoDTO);
        assertEquals(mockUser.getName(), fileInfoDTO.getFileName());
        assertNotNull(fileInfoDTO.getUploadDate());

        verify(mockUserRepository).save(mockUser);

    }




    @Test
    public void testDeleteFile_Success() {
        String username = "Test File";
        User mockUser = findUser(username);
        when(mockUserRepository.findByName(username)).thenReturn(Optional.of(mockUser));

        userServiceUnderTest.deleteFile(username);

        verify(mockUserRepository).delete(mockUser);

    }

    @Test
    public void testDeleteFile_FileNotFound() {
        String username = "dddd";
        when(mockUserRepository.findByName(username)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userServiceUnderTest.deleteFile(username);
        });

        assertTrue(exception.getMessage().contains("User not found: " + username));
    }
}

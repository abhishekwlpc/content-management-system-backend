package org.example.content_upload_system.service;

import lombok.RequiredArgsConstructor;

import org.example.content_upload_system.entity.Content;
import org.example.content_upload_system.entity.Instructor;
import org.example.content_upload_system.exception.CannotExtractInstructorIDFromToken;
import org.example.content_upload_system.exception.FileSizeLimitExceedException;
import org.example.content_upload_system.exception.InvalidInstrctorIDException;
import org.example.content_upload_system.repository.ContentRepository;
import org.example.content_upload_system.repository.InstructorRepository;
import org.example.content_upload_system.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final InstructorRepository instructorRepository;
    private final S3Service s3Service;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Autowired
    private JwtUtil jwtUtil;


    public Content uploadFile(MultipartFile file, String token) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new FileNotFoundException("File is missing or empty");
        }

        if(file.getSize() > 100 * 1024 * 1024) {
            throw new FileSizeLimitExceedException("File size exceeds the maximum allowed limit of 100MB", HttpStatus.CONTENT_TOO_LARGE);
        }

        Integer instructorId = jwtUtil.extractInstructorId(token);
        if (instructorId == null) {
            throw new CannotExtractInstructorIDFromToken("Unable to extract instructor id from token", HttpStatus.BAD_REQUEST);
        }

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new InvalidInstrctorIDException("Instructor not found with id: " + instructorId, HttpStatus.NOT_FOUND));

        String fileUrl = s3Service.uploadFile(file);

        Content content = new Content();
        content.setFileName(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        content.setFileType(file.getContentType());
        content.setFileSize(file.getSize());
        content.setFileUrl(fileUrl);
        content.setUploadDate(LocalDateTime.now());
        content.setOwner(instructor);

        return contentRepository.save(content);
    }


    public List<Content> getMyFiles(String token) {
        return contentRepository.findByOwnerId(jwtUtil.extractInstructorId(token));
    }

    public Content getContentById(Long id) {
        return contentRepository.findById(id).orElse(null);
    }

    public String getDownloadUrl(Long id) throws FileNotFoundException {

        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        String fileUrl = content.getFileUrl();
        if (fileUrl == null || (!fileUrl.contains("s3.amazonaws.com") && !fileUrl.contains("/" + s3Service.getBucketName() + "/"))) {
            return fileUrl;
        }

        try {
            return s3Service.getPresignedUrlForUrl(fileUrl, 300);
        } catch (RuntimeException re) {
            throw new RuntimeException("Failed to generate presigned URL: " + re.getMessage(), re);
        }
    }


}
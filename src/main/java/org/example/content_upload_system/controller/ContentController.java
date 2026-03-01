package org.example.content_upload_system.controller;

import lombok.RequiredArgsConstructor;
import org.example.content_upload_system.entity.Content;
import org.example.content_upload_system.service.ContentService;
import org.example.content_upload_system.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam MultipartFile file,
            @RequestHeader("Authorization") String token
            ) throws IOException {

        return ResponseEntity.ok(
                contentService.uploadFile(file, token)
        );
    }

    @GetMapping("/all")
    public List<Content> getAllFiles(@RequestHeader("Authorization") String token) {
        return contentService.getMyFiles(token);
    }

    @GetMapping("/my/{instructorId}")
    public List<Content> getMyFiles(@RequestHeader("Authorization") String token) {
        return contentService.getMyFiles(token);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable Long id) {

        try {
            String url = contentService.getDownloadUrl(id);
            Map<String, String> body = new HashMap<>();
            body.put("url", url);
            return ResponseEntity.ok(body);
        } catch (RuntimeException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("error", ex.getMessage());
            return ResponseEntity.status(404).body(body);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{id}/debug")
    public ResponseEntity<?> debug(@PathVariable Long id) {
        Content content = contentService.getContentById(id);
        if (content == null) {
            return ResponseEntity.notFound().build();
        }

        String fileUrl = content.getFileUrl();
        String s3Key = s3Service.extractKeyFromUrl(fileUrl);
        boolean exists = (s3Key != null) && s3Service.objectExists(s3Key);

        Map<String, Object> resp = new HashMap<>();
        resp.put("fileUrl", fileUrl);
        resp.put("s3Key", s3Key);
        resp.put("exists", exists);

        return ResponseEntity.ok(resp);
    }
}
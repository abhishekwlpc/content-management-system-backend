package org.example.content_upload_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public String getBucketName() {
        return bucketName;
    }

    public String uploadFile(MultipartFile file) throws IOException {

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(file.getBytes())
        );

        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }

    public String getPresignedUrl(String s3Key, long expirationSeconds) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofSeconds(expirationSeconds))
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    public boolean objectExists(String s3Key) {
        try {
            HeadObjectRequest headReq = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            HeadObjectResponse resp = s3Client.headObject(headReq);
            return resp != null;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    public String getPresignedUrlForUrl(String fileUrl, long expirationSeconds) {
        if (fileUrl == null) {
            throw new IllegalArgumentException("fileUrl is null");
        }

        try {
            URL url = new URL(fileUrl);
            String host = url.getHost();
            String path = url.getPath();

            String keyCandidate = path.startsWith("/") ? path.substring(1) : path;

            String bucketPrefix = bucketName + "/";
            if (keyCandidate.startsWith(bucketPrefix)) {
                keyCandidate = keyCandidate.substring(bucketPrefix.length());
            }

            if (host.startsWith(bucketName + ".")) {
            } else {
                int idx = path.indexOf("/" + bucketName + "/");
                if (idx >= 0) {
                    keyCandidate = path.substring(idx + 1 + bucketName.length() + 1);
                }
            }

            String s3Key = URLDecoder.decode(keyCandidate, StandardCharsets.UTF_8.name());

            if (!objectExists(s3Key)) {
                throw new RuntimeException("S3 object not found for key: " + s3Key);
            }

            return getPresignedUrl(s3Key, expirationSeconds);

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            int idx = fileUrl.indexOf("s3.amazonaws.com/");
            if (idx >= 0) {
                String candidate = fileUrl.substring(idx + "s3.amazonaws.com/".length());
                try {
                    String decoded = URLDecoder.decode(candidate, StandardCharsets.UTF_8.name());
                    if (!objectExists(decoded)) {
                        throw new RuntimeException("S3 object not found for key: " + decoded);
                    }
                    return getPresignedUrl(decoded, expirationSeconds);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to generate presigned URL: " + ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    public String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null) return null;
        try {
            URL url = new URL(fileUrl);
            String host = url.getHost();
            String path = url.getPath();

            String keyCandidate = path.startsWith("/") ? path.substring(1) : path;

            String bucketPrefix = bucketName + "/";
            if (keyCandidate.startsWith(bucketPrefix)) {
                keyCandidate = keyCandidate.substring(bucketPrefix.length());
            }

            if (!host.startsWith(bucketName + ".")) {
                int idx = path.indexOf("/" + bucketName + "/");
                if (idx >= 0) {
                    keyCandidate = path.substring(idx + 1 + bucketName.length() + 1);
                }
            }

            return URLDecoder.decode(keyCandidate, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            int idx = fileUrl.indexOf("s3.amazonaws.com/");
            if (idx >= 0) {
                String candidate = fileUrl.substring(idx + "s3.amazonaws.com/".length());
                try {
                    return URLDecoder.decode(candidate, StandardCharsets.UTF_8.name());
                } catch (Exception ex) {
                    return candidate;
                }
            }
            return null;
        }
    }
}
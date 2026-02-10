package com.abhisekhsite.Authentication.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


@Service
public class S3Service {

    private final S3Client s3;
    private final String bucket;

    public S3Service(
            @Value("${app.aws.region}") String region,
            @Value("${app.aws.s3.bucket}") String bucket
    ) {
        this.bucket = bucket;

        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .build(); // AWS SDK reads ENV vars automatically
    }

    public String uploadFile(MultipartFile file) throws Exception {
        String key = UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3.putObject(request, RequestBody.fromBytes(file.getBytes()));
        return key;
    }

    public String getFileUrl(String key) {
        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }
}



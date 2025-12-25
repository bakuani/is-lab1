package ru.ani.islab1.services;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @SneakyThrows
    public void uploadFile(String objectName, InputStream inputStream, String contentType) {
        String safeContentType = (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, -1, 10485760)
                        .contentType(safeContentType)
                        .build());
    }

    @SneakyThrows
    public InputStream getFile(String objectName) {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }

    @SneakyThrows
    public void deleteFile(String objectName) {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }

    @SneakyThrows
    public void copyFile(String sourceObjectName, String targetObjectName) {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucketName)
                        .object(targetObjectName)
                        .source(CopySource.builder().bucket(bucketName).object(sourceObjectName).build())
                        .build());
    }
}
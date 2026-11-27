package com.comecome.cadastro.services;

import com.comecome.cadastro.exceptions.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
public class BlobStorageService {

    // O prefixo users/ é público no bucket; aceitar só imagens evita que ele
    // seja usado para servir HTML ou outros conteúdos arbitrários.
    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final S3Client s3Client;
    private final String bucket;

    public BlobStorageService(S3Client s3Client, @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String uploadFile(String fileName, InputStream data, long size, String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException("Formato de imagem não suportado. Use JPEG, PNG ou WebP.");
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(data, size));

        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(fileName).build())
                .toExternalForm();
    }

    public void deleteProfilePicture(UUID userId) {
        // Reconstrói o caminho do arquivo (mesma convenção do upload)
        String path = "users/" + userId + "/profile.jpg";

        // No S3 o DeleteObject não falha se o objeto não existir.
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build());
    }
}

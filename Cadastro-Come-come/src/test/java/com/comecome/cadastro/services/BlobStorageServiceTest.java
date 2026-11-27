package com.comecome.cadastro.services;

import com.comecome.cadastro.exceptions.InvalidFileTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlobStorageServiceTest {

    private static final String BUCKET = "comecome-test-bucket";

    @Mock
    private S3Client s3Client;

    private BlobStorageService service;

    @BeforeEach
    void setUp() {
        service = new BlobStorageService(s3Client, BUCKET);
    }

    @Test
    @DisplayName("uploadFile envia o objeto ao bucket com o content-type e devolve a URL pública")
    void uploadFile_success() {
        when(s3Client.utilities()).thenReturn(S3Utilities.builder().region(Region.US_EAST_1).build());
        byte[] bytes = {1, 2, 3};

        String url = service.uploadFile("users/abc/profile.jpg", new ByteArrayInputStream(bytes), bytes.length, "image/png");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("users/abc/profile.jpg", captor.getValue().key());
        assertEquals("image/png", captor.getValue().contentType());
        assertEquals("https://" + BUCKET + ".s3.amazonaws.com/users/abc/profile.jpg", url);
    }

    @Test
    @DisplayName("uploadFile rejeita arquivos que não são imagem sem chamar o S3")
    void uploadFile_rejectsInvalidContentType() {
        byte[] bytes = "<script>".getBytes();

        assertThrows(InvalidFileTypeException.class, () ->
                service.uploadFile("users/abc/profile.jpg", new ByteArrayInputStream(bytes), bytes.length, "text/html"));
        assertThrows(InvalidFileTypeException.class, () ->
                service.uploadFile("users/abc/profile.jpg", new ByteArrayInputStream(bytes), bytes.length, null));

        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("deleteProfilePicture remove o objeto do usuário no bucket")
    void deleteProfilePicture_success() {
        UUID userId = UUID.randomUUID();

        service.deleteProfilePicture(userId);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals("users/" + userId + "/profile.jpg", captor.getValue().key());
    }
}

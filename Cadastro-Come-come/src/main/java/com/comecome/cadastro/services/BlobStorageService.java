package com.comecome.cadastro.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class BlobStorageService {

    private final BlobContainerClient blobContainerClient;

    public BlobStorageService(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    public String uploadFile(String fileName, InputStream data, long size){
        BlobClient client = blobContainerClient.getBlobClient(fileName);
        client.upload(data, size, true);

        return client.getBlobUrl();
    }

    public void deleteProfilePicture(UUID userId) {
        // Reconstrói o caminho do arquivo (baseado na lógica que definimos antes)
        String path = "users/" + userId + "/profile.jpg";

        BlobClient client = blobContainerClient.getBlobClient(path);

        // Tenta deletar. Se não existir, não faz nada e segue a vida.
        client.deleteIfExists();
    }
}
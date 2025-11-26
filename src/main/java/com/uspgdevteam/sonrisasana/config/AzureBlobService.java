package com.uspgdevteam.sonrisasana.config;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Servicio simple para subir archivos clínicos a Azure Blob Storage.
 */
public class AzureBlobService {

    private final BlobContainerClient containerClient;

    public AzureBlobService() {
        String connectionString = AzureEnv.get("AZURE_STORAGE_CONNECTION_STRING", "");
        String containerName = AzureEnv.get("AZURE_STORAGE_CONTAINER", "archivos-clinicos");

        if (connectionString == null || connectionString.isBlank()) {
            containerClient = null;
        } else {
            BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            containerClient = serviceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
            }
        }
    }

    public String upload(InputStream input, long length, String originalFilename) {
        if (containerClient == null) {
            // Modo local sin Azure configurado
            return null;
        }

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String blobName = OffsetDateTime.now().toLocalDate() + "/" + UUID.randomUUID() + extension;
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(input, length, true);

        return blobClient.getBlobUrl();
    }
}

package com.linkage.utility;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.linkage.api.ApiResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public final class GcpFileUpload {

    private static final String SERVICE_ACCOUNT_KEY_PATH = Paths
            .get(System.getProperty("user.dir"), "serviceAccount.json").toString();
    private static final long EXPIRATION_TIMEOUT = 300;
    public static final String USER_DATA_BUCKET = "qube-user-data-encrypted";
    public static final String PUBLIC_APP_DATA = "qube-app-data";

    private GcpFileUpload() {

    }

    private static Storage getStorage() throws IOException {
        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_PATH)))
                .build()
                .getService();
    }

    public static ApiResponse<String> uploadFile(String bucketName, String outputFileNamePath, String fileContent,
            String contentType, boolean isSignUrl) {
        try {
            Storage storage = getStorage();
            // Define the path for staging environment
            if (!"PROD".equals(System.getenv("Environment"))) {
                outputFileNamePath = "testing/" + outputFileNamePath;
            }

            BlobId blobId = BlobId.of(bucketName, outputFileNamePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            Blob blob = storage.create(blobInfo, fileContent.getBytes());

            // Handle signed URL generation
            String signedUrl = "";
            if (isSignUrl) {
                signedUrl = blob.signUrl(EXPIRATION_TIMEOUT, TimeUnit.DAYS).toString();
            }

            return new ApiResponse<>(true, "File Uploaded Successfully", signedUrl);
        } catch (Exception e) {
            return new ApiResponse<>(false, "File Upload Failed", e.getMessage());
        }
    }

    public static String getSignedUrl(String bucketName, String fileName) {
        try {

            fileName = "testing/" + fileName;

            // Get a reference to the bucket
            Storage storage = getStorage();
            Blob blob = storage.get(bucketName, fileName);

            // Create a signed URL with the specified expiration time
            String signedUrl = "";
            if (blob != null) {
                signedUrl = blob.signUrl(EXPIRATION_TIMEOUT, TimeUnit.MINUTES).toString();
            }

            return signedUrl;

        } catch (Exception e) {
            System.err.println("GET SIGN URL ERROR => " + e.getMessage());
            return "";
        }
    }

    public static ApiResponse<String> uploadEmailAttachments(String bucketName, String outputFileNamePath,
            byte[] fileContent, String contentType, boolean isSignUrl) throws Exception {
        try {
            Storage storage = getStorage();

            outputFileNamePath = "testing/" + outputFileNamePath;

            BlobId blobId = BlobId.of(bucketName, outputFileNamePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            Blob blob = storage.create(blobInfo, fileContent);

            // Handle signed URL generation
            String signedUrl = "";
            if (isSignUrl) {
                signedUrl = blob.signUrl(EXPIRATION_TIMEOUT, TimeUnit.DAYS).toString();
            }

            return new ApiResponse<>(true, "File Uploaded Successfully", signedUrl);
        } catch (Exception e) {
            // throw e;
            return new ApiResponse<>(false, "File Upload Failed", e.getMessage());
        }
    }
}

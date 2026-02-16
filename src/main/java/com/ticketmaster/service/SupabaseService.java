package com.ticketmaster.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;  // <-- Use this for Spring Boot 3.x
import java.io.IOException;

@Service
public class SupabaseService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("SUPABASE SERVICE INITIALIZED");
        System.out.println("URL: " + supabaseUrl);
        System.out.println("Bucket: " + bucketName);
        System.out.println("Key loaded: " + (supabaseKey != null && !supabaseKey.isEmpty() ? "YES" : "NO"));
        if (supabaseKey != null && supabaseKey.length() > 10) {
            System.out.println("Key preview: " + supabaseKey.substring(0, 15) + "...");
        }
        System.out.println("========================================");
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        System.out.println("=== SUPABASE UPLOAD ATTEMPT ===");
        System.out.println("Upload URL: " + uploadUrl);
        System.out.println("File: " + fileName + " (" + file.getSize() + " bytes)");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println("✓ Upload SUCCESS - Status: " + response.getStatusCode());

            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
            System.out.println("✓ Public URL: " + publicUrl);
            return publicUrl;

        } catch (HttpClientErrorException e) {
            System.err.println("✗ Upload FAILED - Status: " + e.getStatusCode());
            System.err.println("✗ Error: " + e.getResponseBodyAsString());
            throw new IOException("Supabase upload failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("✗ Upload EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Supabase upload failed: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
            System.out.println("✓ File deleted: " + fileName);
        } catch (Exception e) {
            System.err.println("✗ Delete failed: " + e.getMessage());
        }
    }
}
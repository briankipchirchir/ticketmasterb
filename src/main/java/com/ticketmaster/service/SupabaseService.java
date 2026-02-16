package com.ticketmaster.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        System.out.println("=== SUPABASE UPLOAD DEBUG ===");
        System.out.println("Upload URL: " + uploadUrl);
        System.out.println("Bucket: " + bucketName);
        System.out.println("File name: " + fileName);
        System.out.println("File size: " + file.getSize() + " bytes");
        System.out.println("Content type: " + file.getContentType());
        System.out.println("API Key (first 10 chars): " + supabaseKey.substring(0, Math.min(10, supabaseKey.length())) + "...");

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

            System.out.println("Upload SUCCESS!");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());

            // Return public URL
            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
            System.out.println("Public URL: " + publicUrl);
            return publicUrl;

        } catch (HttpClientErrorException e) {
            System.err.println("Upload FAILED!");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            throw new IOException("Failed to upload to Supabase: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Upload FAILED with exception!");
            e.printStackTrace();
            throw new IOException("Failed to upload to Supabase: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        System.out.println("Deleting file: " + deleteUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
            System.out.println("Delete SUCCESS");
        } catch (Exception e) {
            System.err.println("Failed to delete from Supabase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
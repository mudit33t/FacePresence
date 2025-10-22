package com.org.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.org.service.FaceService;

@RestController
@RequestMapping("/api/face")
public class FaceController {

    @Autowired
    private FaceService faceService;

    // Welcome endpoint
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Facial Attendance System API is running!\n\n" +
                "Available endpoints:\n" +
                "• GET /api/face/capture/{userId}/{numImages} - Capture training images\n" +
                "• GET /api/face/train - Train the face recognition model\n" +
                "• GET /api/face/mark-attendance - Mark attendance from camera\n" +
                "• POST /api/face/mark-attendance-from-image - Mark attendance from uploaded image\n" +
                "• POST /api/face/upload-menu-image/{userId} - Upload menu image\n" +
                "• GET /api/face/menu-images/{userId}/view/{imageName} - View menu images\n" +
                "• GET /api/face/clear-images/{userId} - Clear training images\n\n" +
                "H2 Database Console: http://localhost:9090/h2-console");
    }

    // Root endpoint
    @GetMapping("/api")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Facial Attendance System API is running!\n\n" +
                "Available endpoints:\n" +
                "• GET /api/face/capture/{userId}/{numImages} - Capture training images\n" +
                "• GET /api/face/train - Train the face recognition model\n" +
                "• GET /api/face/mark-attendance - Mark attendance from camera\n" +
                "• POST /api/face/mark-attendance-from-image - Mark attendance from uploaded image\n" +
                "• POST /api/face/upload-menu-image/{userId} - Upload menu image\n" +
                "• GET /api/face/menu-images/{userId}/view/{imageName} - View menu images\n" +
                "• GET /api/face/clear-images/{userId} - Clear training images\n\n" +
                "H2 Database Console: http://localhost:9090/h2-console");
    }

    // Capture Training Images
    @GetMapping("/capture/{userId}/{numImages}")
    public ResponseEntity<String> captureTrainingImages(@PathVariable int userId, @PathVariable int numImages) {
        try {
            String result = faceService.captureTrainingImages(userId, numImages);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Capture error: " + e.getMessage());
        }
    }

    // Train Model
    @GetMapping("/train")
    public ResponseEntity<String> trainModel() {
        try {
            faceService.trainModel();
            return ResponseEntity.ok("Model trained and saved as lbph_model.xml");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Training error: " + e.getMessage());
        }
    }

    // Mark Attendance from Camera
    @GetMapping("/mark-attendance")
    public ResponseEntity<String> markAttendance() {
        String result = faceService.recognizeAndMark();
        return ResponseEntity.ok(result);
    }

    // Mark Attendance from Uploaded Image
    @PostMapping("/mark-attendance-from-image")
    public ResponseEntity<String> markAttendanceFromImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            String result = faceService.recognizeFromImage(imageFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // Upload Menu Image
    @PostMapping("/upload-menu-image/{userId}")
    public ResponseEntity<String> uploadMenuImage(@RequestParam("menuImage") MultipartFile menuImage,
            @PathVariable int userId) {
        try {
            String result = faceService.uploadMenuImage(menuImage, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // View Menu Image
    @GetMapping("/menu-images/{userId}/view/{imageName}")
    public ResponseEntity<Resource> viewMenuImage(@PathVariable int userId, @PathVariable String imageName) {
        try {
            Path imagePath = Paths.get("C:/menu_images/user" + userId + "/", imageName);

            if (!Files.exists(imagePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Resource fileResource = new UrlResource(imagePath.toUri());

            String contentType = Files.probeContentType(imagePath); // auto detect content type like jpeg/png/gif etc

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageName + "\"")
                    .contentType(
                            MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .body(fileResource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Clear Training Images
    @GetMapping("/clear-images/{userId}")
    public ResponseEntity<String> clearImages(@PathVariable int userId) {
        String folderPath = "C:/training_images/user" + userId + "/";
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
            return ResponseEntity.ok("Images for user " + userId + " have been deleted.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Folder not found for user: " + userId);
        }
    }
}

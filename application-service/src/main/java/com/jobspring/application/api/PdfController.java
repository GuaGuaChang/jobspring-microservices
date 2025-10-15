package com.jobspring.application.api;

import com.jobspring.application.entity.PdfFile;
import com.jobspring.application.repository.PdfRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.Binary;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PdfController {

    private final PdfRepository repo;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestPart("file") MultipartFile file,
                                                      UriComponentsBuilder uriBuilder) throws IOException {
        String publicId = UUID.randomUUID().toString().replace("-", "");

        PdfFile pdf = PdfFile.builder()
                .publicId(publicId)
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .data(new org.bson.types.Binary(file.getBytes()))
                .uploadAt(Instant.now())
                .build();

        repo.save(pdf);

        String url = uriBuilder
                .path("/api/files/d/{pid}")
                .buildAndExpand(publicId)
                .toUriString();

        return ResponseEntity.ok(Map.of(
                "publicId", publicId,
                "url", url
        ));
    }


    @GetMapping("/download/{pid}")
    public void downloadByPublicId(@PathVariable("pid") String publicId,
                                   HttpServletResponse response) throws IOException {
        PdfFile pdf = repo.findByPublicId(publicId).orElse(null);
        if (pdf == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }


        String ct = pdf.getContentType();
        response.setContentType((ct == null || ct.isBlank()) ? "application/pdf" : ct);

        String filename = pdf.getFilename() == null ? "file.pdf" : pdf.getFilename();
        String contentDisp = ContentDisposition.inline()
                .filename(filename, StandardCharsets.UTF_8)
                .build().toString();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisp);

        response.getOutputStream().write(pdf.getData().getData());
    }

//    @GetMapping("/download/{id}")
//    public void download(@PathVariable String id, HttpServletResponse response) throws IOException {
//        PdfFile pdf = repo.findById(id).orElse(null);
//        if (pdf == null) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//        response.setContentType(pdf.getContentType());
//        response.setHeader("Content-Disposition", "inline; filename=\"" + pdf.getFilename() + "\"");
//        response.getOutputStream().write(pdf.getData().getData());
//    }
}

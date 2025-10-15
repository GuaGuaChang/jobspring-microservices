package com.jobspring.application.repository;

import com.jobspring.application.entity.PdfFile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface PdfRepository extends MongoRepository<PdfFile, String> {
    Optional<PdfFile> findByPublicId(String publicId);
}
package com.jobspring.application.entity;


import lombok.Builder;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document("pdf_files")
public class PdfFile {
    @Id
    private String id;

    @Indexed(unique = true)
    private String publicId;
    private String filename;
    private String contentType;
    private Binary data;
    private Instant uploadAt;

}

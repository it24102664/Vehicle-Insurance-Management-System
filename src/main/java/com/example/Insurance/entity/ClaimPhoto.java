package com.example.Insurance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "claim_photos")
public class ClaimPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    // Constructors
    public ClaimPhoto() {}

    public ClaimPhoto(String fileName, String filePath, String description, Claim claim) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.description = description;
        this.claim = claim;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }
}

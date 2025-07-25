package com.skillvault.backend.Domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.skillvault.backend.Domain.Enums.SkillStatus;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private SkillStatus status = SkillStatus.PENDING;

    private boolean isFeatured;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    @JsonBackReference
    private Certificate certificate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    public Skill(SkillRequestDTO dto, User user){
        this.name = dto.name();
        this.description = dto.description();
        this.user = user;
        this.isFeatured = dto.featured() != null ? dto.featured() : false;
    }

    public boolean validateEquality(SkillStatus status){
        return this.status.equals(status);
    }

}

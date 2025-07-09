package com.skillvault.backend.Domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User evaluator;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User evaluatedUser;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id", unique = true)
    private Certificate certificate;

    @ManyToMany
    @JsonBackReference
    private List<Skill> approvedSkills = new ArrayList<>();

    @ManyToMany
    @JsonBackReference
    private List<Skill> reprovedSkills = new ArrayList<>();

    private String obs;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @AssertTrue(message = "If there are reproved skills, you must provide an observation.")
    public boolean isValidObservation(){
        return reprovedSkills == null || reprovedSkills.isEmpty() || (obs != null && !obs.isBlank());
    }
}

package com.skillvault.backend.Domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skillvault.backend.Domain.Enums.EvalResult;
import jakarta.persistence.*;
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
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private EvalResult status = EvalResult.PENDING;

    private boolean isFeatured;

    @JsonBackReference
    @ManyToOne
    private User user;

    @JsonManagedReference
    @OneToMany(mappedBy = "certificate",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Skill> requestedSkills = new ArrayList<>();

    @OneToOne(mappedBy = "certificate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Evaluation evaluation;

    @Column(unique = true)
    private String blobName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    public void addSkills(List<Skill> requestedSkills){
        this.requestedSkills.addAll(requestedSkills);
    }

}

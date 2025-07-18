package com.skillvault.backend.Domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Validations.Password.Password;
import com.skillvault.backend.dtos.Requests.UpdateUserDTO;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;


    @Password
    @Column(nullable = false)
    private String password;

    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfilePicture profilePicture;

    @Column(length = 1000)
    private String biography;

    private String linkedin;

    private String github;

    private String site;

    @Column(nullable = false)
    private UserRole role;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Certificate> certificates = new ArrayList<>();

    @JsonBackReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.role.getPermissions().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public void updateFromDTO(UpdateUserDTO dto) {
        if (dto.name() != null && !dto.name().isBlank()) this.name = dto.name();
        if (dto.email() != null && !dto.email().isBlank()) this.email = dto.email();
        if (dto.biography() != null) this.biography = dto.biography();
        if (dto.linkedin() != null) this.linkedin = dto.linkedin();
        if (dto.github() != null) this.github = dto.github();
        if (dto.site() != null) this.site = dto.site();

    }
    public User(UserRequestDTO dto, UserRole role){
        this.username = dto.username();
        this.email = dto.email();
        this.name = dto.name();
        this.role = role;
    }

}

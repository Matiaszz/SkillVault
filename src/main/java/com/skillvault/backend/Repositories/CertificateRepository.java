package com.skillvault.backend.Repositories;

import com.skillvault.backend.Domain.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    Optional<Page<Certificate>> findByUserId(UUID userId, Pageable pageable);
}

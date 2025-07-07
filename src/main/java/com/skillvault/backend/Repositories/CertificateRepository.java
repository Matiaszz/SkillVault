package com.skillvault.backend.Repositories;

import com.skillvault.backend.Domain.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
}

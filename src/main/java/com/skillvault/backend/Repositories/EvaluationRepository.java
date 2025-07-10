package com.skillvault.backend.Repositories;

import com.skillvault.backend.Domain.Evaluation;
import com.skillvault.backend.Domain.User;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNullApi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {

    Page<Evaluation> findByEvaluator_Id(UUID id, Pageable pageable);

    Page<Evaluation> findByEvaluatedUser_Id(UUID userId, Pageable pageable);

    Optional<Evaluation> findByCertificate_Id(UUID id);

    Page<Evaluation> findByEvaluatedUser(User user, Pageable pageable);

    @Override
    @NonNull
    Page<Evaluation> findAll(@NonNull Pageable pageable);
}

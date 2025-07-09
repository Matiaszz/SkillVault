package com.skillvault.backend.Repositories;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Evaluation;
import com.skillvault.backend.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
    Evaluation findByEvaluatorAndCertificate(User evaluator, Certificate certificate);
    Optional<List<Evaluation>> findByEvaluator_Id(UUID id);
    Optional<Evaluation> findByCertificate_Id(UUID id);
    Optional<List<Evaluation>> findByEvaluatedUser(User user);
}

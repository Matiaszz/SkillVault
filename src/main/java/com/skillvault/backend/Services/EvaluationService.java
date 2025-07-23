package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.EvalResult;
import com.skillvault.backend.Domain.Enums.SkillStatus;
import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.Evaluation;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.CertificateRepository;
import com.skillvault.backend.Repositories.EvaluationRepository;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Requests.EvaluationRequestDTO;
import com.skillvault.backend.dtos.Responses.EvaluationResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final EvaluationRepository evaluationRepository;
    private final SkillService skillService;
    private final TokenService tokenService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @Transactional
    public EvaluationResponseDTO createEvaluation(UUID certificateId, EvaluationRequestDTO data){
        User loggedUser = tokenService.getLoggedEntity();

        if (loggedUser.getRole().equals(UserRole.USER)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "An user cannot access evaluation area.");
        }

        Certificate certificate = certificateService.getCertificateById(certificateId);

        if (certificate.getEvaluation() != null){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This certificate already have an evaluation.");
        }

        skillService.verifyLinkBetweenSkillAndCertificate(data.approvedSkills(), certificate);
        skillService.verifyLinkBetweenSkillAndCertificate(data.reprovedSkills(), certificate);

        List<Skill> approvedSkills = data.approvedSkills().stream()
                .filter(skill -> SkillStatus.PENDING.name().equalsIgnoreCase(skill.status()))
                .map(skill -> skillService.changeSkillStatus(skill, SkillStatus.APPROVED)
        ).toList();



        List<Skill> reprovedSkills = data.reprovedSkills().stream()
                .filter(skill -> SkillStatus.PENDING.name().equalsIgnoreCase(skill.status()))
                .map(skill -> skillService.changeSkillStatus(skill, SkillStatus.REPROVED)
                ).toList();

        for (Skill skill: certificate.getRequestedSkills()){
            if (skill.validateEquality(SkillStatus.PENDING)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "You as an evaluator must evaluate all the skills, some skills is missing.");

            }
        }

        EvalResult evalResult = certificateService.determineEvalResult(approvedSkills, reprovedSkills);
        certificate.setStatus(evalResult);

        certificateRepository.save(certificate);
        certificateRepository.flush();

        if (data.title() == null || data.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required.");
        }
        LocalDateTime now = LocalDateTime.now();
        Evaluation evaluationBuild = Evaluation.builder()
                .title(data.title())
                .evaluator(loggedUser)
                .evaluatedUser(certificate.getUser())
                .approvedSkills(approvedSkills)
                .reprovedSkills(reprovedSkills)
                .certificate(certificate)
                .obs(data.obs())
                .createdAt(now)
                .updatedAt(now)
                .build();

        if (!reprovedSkills.isEmpty() && (data.obs() == null || data.obs().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Observation is required for reproved skills.");
        }

        Evaluation evaluation = evaluationRepository.save(evaluationBuild);
        notificationService.notifyUserAboutEvaluation(evaluation);
        return new EvaluationResponseDTO(evaluation);
    }

    public Page<Evaluation> getEvaluationsByUserId(UUID id, Pageable pageable){

        User targetUser = userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        return evaluationRepository.findByEvaluatedUser(targetUser, pageable);

    }

    public Page<Evaluation> getAllEvaluations(Pageable pageable){
        return evaluationRepository.findAll(pageable);
    }

    public Page<Evaluation> getEvaluationsByEvaluatorId(UUID id, Pageable pageable){
        Page<Evaluation> evaluations = evaluationRepository.findByEvaluator_Id(id, pageable);

        if (evaluations.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This evaluator don't have any evaluation.");

        return evaluations;
    }

    public Evaluation getEvaluationByCertificateId(UUID id){
        Evaluation evaluation = evaluationRepository.findByCertificate_Id(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation not found."));

        User loggedUser = tokenService.getLoggedEntity();

        if (!loggedUser.getId().equals(evaluation.getEvaluatedUser().getId())
                && !loggedUser.getRole().equals(UserRole.EVALUATOR)
                && !loggedUser.getRole().equals(UserRole.ADMIN)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access other people evaluations.");
        }

        return evaluation;
    }
}

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
import com.skillvault.backend.Repositories.SkillRepository;
import com.skillvault.backend.dtos.Requests.EvaluationRequestDTO;
import com.skillvault.backend.dtos.Responses.EvaluationResponseDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final EmailService emailService;
    private final EvaluationRepository evaluationRepository;
    private final SkillService skillService;
    private final TokenService tokenService;


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
            if (skill.getStatus().equals(SkillStatus.PENDING)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "You as an evaluator must evaluate all the skills, some skills is missing.");

            }
        }

        EvalResult evalResult = certificateService.determineEvalResult(approvedSkills, reprovedSkills);
        certificate.setStatus(evalResult);

        certificateRepository.save(certificate);
        certificateRepository.flush();

        Evaluation evaluationBuild = Evaluation.builder()
                .title(data.title())
                .evaluator(loggedUser)
                .evaluatedUser(certificate.getUser())
                .approvedSkills(approvedSkills)
                .reprovedSkills(reprovedSkills)
                .certificate(certificate)
                .obs(data.obs())
                .build();

        Evaluation evaluation = evaluationRepository.save(evaluationBuild);
        emailService.notifyAdminsAboutEvaluationCompleted(evaluation);
        return new EvaluationResponseDTO(evaluation);
    }
}

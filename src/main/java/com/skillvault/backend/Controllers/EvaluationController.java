package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Evaluation;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.EvaluationService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.EvaluationRequestDTO;
import com.skillvault.backend.dtos.Responses.EvaluationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final TokenService tokenService;

    @GetMapping("/evaluator")
    public ResponseEntity<Page<EvaluationResponseDTO>> getEvaluationByLoggedEvaluator(Pageable pageable){
        UUID evaluatorId = tokenService.getLoggedEntity().getId();
        Page<Evaluation> evaluations = evaluationService.getEvaluationsByEvaluatorId(evaluatorId, pageable);
        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EVALUATOR')")
    @GetMapping("/by-evaluator/{evaluatorId}")
    public ResponseEntity<Page<EvaluationResponseDTO>> getEvaluationsByEvaluatorId(@PathVariable UUID evaluatorId, Pageable pageable) {
        Page<Evaluation> evaluations = evaluationService.getEvaluationsByEvaluatorId(evaluatorId, pageable);

        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EVALUATOR') or #userId == authentication.principal.id")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Page<EvaluationResponseDTO>> getEvaluationsByUserId(@PathVariable UUID userId, Pageable pageable){
        Page<Evaluation> evaluations = evaluationService.getEvaluationsByUserId(userId, pageable);
        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<EvaluationResponseDTO>> getAllEvaluations(Pageable pageable){
        Page<Evaluation> evaluations = evaluationService.getAllEvaluations(pageable);

        if (evaluations == null || evaluations.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        Page<EvaluationResponseDTO> evaluationResponseDTOS = evaluations
                .map(EvaluationResponseDTO::new);
        return ResponseEntity.ok(evaluationResponseDTOS);
    }


    @GetMapping("/my")
    public ResponseEntity<Page<EvaluationResponseDTO>> getEvaluationsByLoggedUser(Pageable pageable){
        UUID userId = tokenService.getLoggedEntity().getId();
        Page<Evaluation> evaluations = evaluationService.getEvaluationsByUserId(userId, pageable);
        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @GetMapping("/by-certificate/{certificateId}")
    public ResponseEntity<EvaluationResponseDTO> getEvaluationByCertificateId(@PathVariable UUID certificateId){
        Evaluation dto = evaluationService.getEvaluationByCertificateId(certificateId);
        return ResponseEntity.ok(new EvaluationResponseDTO(dto));
    }

    @PostMapping("/{certificateId}")
    public ResponseEntity<EvaluationResponseDTO> createEvaluation(@PathVariable UUID certificateId,
                                                                  @RequestBody EvaluationRequestDTO data){
        EvaluationResponseDTO evaluation = evaluationService.createEvaluation(certificateId, data);
        return ResponseEntity.ok(evaluation);
    }
}

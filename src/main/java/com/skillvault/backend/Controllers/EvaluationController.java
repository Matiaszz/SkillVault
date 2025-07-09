package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Evaluation;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.EvaluationService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.EvaluationRequestDTO;
import com.skillvault.backend.dtos.Responses.EvaluationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<?>> getEvaluationByLoggedEvaluator(){
        UUID evaluatorId = tokenService.getLoggedEntity().getId();
        return ResponseEntity.ok(evaluationService.getEvaluationsByEvaluatorId(evaluatorId));
    }

    @GetMapping("/evaluator/{evaluatorId}")
    public ResponseEntity<List<?>> getEvaluationByEvaluatorId(@PathVariable UUID evaluatorId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByEvaluatorId(evaluatorId));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getEvaluationByLoggedUser(){
        User user = tokenService.getLoggedEntity();
        List<Certificate> certificates = user.getCertificates();

        if (certificates == null || certificates.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        List<EvaluationResponseDTO> evaluations = certificates.stream()
                .filter(Objects::nonNull)
                .map(c -> new EvaluationResponseDTO(c.getEvaluation())).toList();


        if (evaluations.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(evaluations);

    }



    @GetMapping("/certificate/{certificateId}")
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

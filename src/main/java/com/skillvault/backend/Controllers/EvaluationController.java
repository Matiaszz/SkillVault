package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Evaluation;
import com.skillvault.backend.Services.EvaluationService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.EvaluationRequestDTO;
import com.skillvault.backend.dtos.Responses.EvaluationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final TokenService tokenService;

    @Operation(summary = "Get evaluations by logged-in evaluator")
    @ApiResponse(responseCode = "200", description = "Evaluations retrieved successfully")
    @GetMapping("/evaluator")
    public ResponseEntity<Page<EvaluationResponseDTO>> getEvaluationByLoggedEvaluator(Pageable pageable) {
        UUID evaluatorId = tokenService.getLoggedEntity().getId();
        Page<Evaluation> evaluations = evaluationService.getEvaluationsByEvaluatorId(evaluatorId, pageable);
        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @Operation(summary = "Get evaluations by evaluator ID (admin or evaluator only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluations retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'EVALUATOR')")
    @GetMapping("/by-evaluator/{evaluatorId}")
    public ResponseEntity<Page<EvaluationResponseDTO>> getEvaluationsByEvaluatorId(
            @Parameter(description = "UUID of the evaluator") @PathVariable UUID evaluatorId,
            Pageable pageable
    ) {
        Page<Evaluation> evaluations = evaluationService.getEvaluationsByEvaluatorId(evaluatorId, pageable);
        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @Operation(summary = "Get evaluations of a specific user (admin, evaluator, or self only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluations retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('EVALUATOR') or #userId == authentication.principal.id")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Page<EvaluationResponseDTO>> getEvaluationsByUserId(
            @Parameter(description = "UUID of the user") @PathVariable UUID userId,
            Pageable pageable
    ) {
        Page<Evaluation> evaluations = evaluationService.getEvaluationsByUserId(userId, pageable);
        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @Operation(summary = "Get all evaluations (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All evaluations retrieved"),
            @ApiResponse(responseCode = "204", description = "No evaluations found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<EvaluationResponseDTO>> getAllEvaluations(Pageable pageable) {
        Page<Evaluation> evaluations = evaluationService.getAllEvaluations(pageable);

        if (evaluations == null || evaluations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @Operation(summary = "Get evaluations by the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Evaluations retrieved")
    @GetMapping("/my")
    public ResponseEntity<Page<EvaluationResponseDTO>> getEvaluationsByLoggedUser(Pageable pageable) {
        UUID userId = tokenService.getLoggedEntity().getId();
        Page<Evaluation> evaluations = evaluationService.getEvaluationsByUserId(userId, pageable);
        return ResponseEntity.ok(evaluations.map(EvaluationResponseDTO::new));
    }

    @Operation(summary = "Get evaluation by certificate ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation found"),
            @ApiResponse(responseCode = "404", description = "Evaluation not found")
    })
    @GetMapping("/by-certificate/{certificateId}")
    public ResponseEntity<EvaluationResponseDTO> getEvaluationByCertificateId(
            @Parameter(description = "UUID of the certificate") @PathVariable UUID certificateId
    ) {
        Evaluation dto = evaluationService.getEvaluationByCertificateId(certificateId);
        return ResponseEntity.ok(new EvaluationResponseDTO(dto));
    }

    @Operation(summary = "Create a new evaluation for a certificate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/{certificateId}")
    public ResponseEntity<EvaluationResponseDTO> createEvaluation(
            @Parameter(description = "UUID of the certificate to evaluate") @PathVariable UUID certificateId,
            @RequestBody EvaluationRequestDTO data
    ) {
        EvaluationResponseDTO evaluation = evaluationService.createEvaluation(certificateId, data);
        return ResponseEntity.ok(evaluation);
    }
}

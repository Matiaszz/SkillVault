package com.skillvault.backend.Controllers;

import com.skillvault.backend.Services.EvaluationService;
import com.skillvault.backend.dtos.Requests.EvaluationRequestDTO;
import com.skillvault.backend.dtos.Responses.EvaluationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/{certificateId}")
    public ResponseEntity<EvaluationResponseDTO> createEvaluation(@PathVariable UUID certificateId,
                                                                  @RequestBody EvaluationRequestDTO data){
        EvaluationResponseDTO evaluation = evaluationService.createEvaluation(certificateId, data);
        return ResponseEntity.ok(evaluation);


    }
}

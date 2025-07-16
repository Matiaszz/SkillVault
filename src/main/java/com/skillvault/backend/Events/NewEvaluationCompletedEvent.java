package com.skillvault.backend.Events;

import com.skillvault.backend.Domain.Evaluation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewEvaluationCompletedEvent extends ApplicationEvent {
    private final Evaluation evaluation;

    public NewEvaluationCompletedEvent(Object source, Evaluation evaluation) {
        super(source);
        this.evaluation = evaluation;
    }

}

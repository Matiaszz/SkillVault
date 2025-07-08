package com.skillvault.backend.Events;

import com.skillvault.backend.Domain.Certificate;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewCertificateUploadedEvent extends ApplicationEvent {
    private final Certificate certificate;

    public NewCertificateUploadedEvent(Object source, Certificate certificate) {
        super(source);
        this.certificate = certificate;
    }
}

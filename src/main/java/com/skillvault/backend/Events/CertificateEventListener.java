package com.skillvault.backend.Events;

import com.skillvault.backend.Services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CertificateEventListener {

    @Autowired
    private EmailService emailService;

    @EventListener
    public void handleNewCertificateUploaded(NewCertificateUploadedEvent event) {
        emailService.notifyEvaluatorsAboutNewCertificate(event.getCertificate());
    }
}
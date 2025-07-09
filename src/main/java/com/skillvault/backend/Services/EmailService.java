package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final String endpoint;

    public EmailService(JavaMailSender mailSender,
                        UserRepository userRepository,
                        @Value("${server.endpoint}") String endpoint) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.endpoint = endpoint;
    }

    public void notifyEvaluatorsAboutNewCertificate(Certificate certificate) {
        String subject = "New certificate available to evaluation";

        String text = " A new certificate sent by " + certificate.getUser().getName()
                + " (" + certificate.getUser().getUsername()
                + ") " + "and is waiting for an evaluation." +
                "\nAccess the following link to download the certificate: " +
                this.endpoint + "/certificate/download/" + certificate.getId().toString();

        sendEmail(subject, text);
    }

    private void sendEmail(String subject, String text){
        List<User> evaluators = userRepository.findByRole(UserRole.EVALUATOR);

        for (User evaluator : evaluators) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(evaluator.getEmail());
            message.setSubject(subject);
            message.setText("Hello " + evaluator.getName() + ",\n\n" +text);

            mailSender.send(message);
        }
    }
}

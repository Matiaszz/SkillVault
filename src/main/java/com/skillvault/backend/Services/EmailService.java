package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

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

    public void notifyEvaluators(Certificate certificate) {
        List<User> evaluators = userRepository.findByRole(UserRole.EVALUATOR);


        for (User evaluator : evaluators) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(evaluator.getEmail());
            message.setSubject("New certificate available to evaluation");
            message.setText("Hello " + evaluator.getName() + ",\n\n" +
                    "A new certificate sent by " + certificate.getUser().getName()
                    + " (" + certificate.getUser().getUsername()
                    + ") " + "and is waiting for an evaluation." +
                    "\nAccess the following link to download the certificate: " +
                    this.endpoint + "/certificate/download/" + certificate.getId().toString());

            mailSender.send(message);
        }
    }
}

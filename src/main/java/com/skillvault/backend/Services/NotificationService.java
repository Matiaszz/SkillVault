package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.Evaluation;
import com.skillvault.backend.Domain.Notification;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.NotificationRepository;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Requests.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Value("${server.endpoint}")
    private String endpoint;

    public void notifyByRoleAboutCertificate(Certificate certificate, UserRole role) {
        List<User> users = userRepository.findByRole(role);
        String title = "New certificate available to evaluation";

        for (User user : users) {
            String message = buildCertificateNotificationText(user, certificate);
            registerNotification(new NotificationRequestDTO(user.getId(), title, message));
        }
    }

    public void notifyUserAboutEvaluation(Evaluation evaluation){
        User evaluatedUser = evaluation.getEvaluatedUser();
        String title = "Evaluation #" + evaluation.getId() + ": status updated";
        String message = buildEvaluationStatusUpdatedNotificationText(evaluation);

        NotificationRequestDTO request = new NotificationRequestDTO(
                evaluatedUser.getId(), title, message
        );

        registerNotification(request);
    }

    private String buildCertificateNotificationText(User recipient, Certificate certificate) {
        User sender = certificate.getUser();
        return "Hello, " + recipient.getName() + ".\n\n" +
                "A new certificate was sent by " + sender.getName() +
                " (" + sender.getUsername() + ") and is waiting for an evaluation.\n" +
                "Access the following link to download the certificate:\n" +
                endpoint + "/certificate/download/" + certificate.getBlobName();
    }

    private String buildEvaluationStatusUpdatedNotificationText(Evaluation evaluation){
        User evaluator = evaluation.getEvaluator();
        Certificate certificate = evaluation.getCertificate();

        return "Hello, " + evaluation.getEvaluatedUser().getName() + ".\n\n" +
                "Your certificate \"" + certificate.getName() + "\" has just been evaluated by " +
                evaluator.getName() + " (" + evaluator.getUsername() + ").\n" +
                "Evaluation title: " + evaluation.getTitle() + "\n\n" +
                "You can view this evaluation by accessing the system.";
    }


    private void registerNotification(NotificationRequestDTO data) {
        Notification notification = Notification.builder()
                .title(data.title())
                .message(data.message())
                .userId(data.userId())
                .build();

        notificationRepository.save(notification);
    }

    public Page<Notification> getNotificationsByUserId(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID doesn't exist");
        }
        return notificationRepository.findByUserId(userId, pageable);
    }
}

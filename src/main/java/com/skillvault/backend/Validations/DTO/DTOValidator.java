package com.skillvault.backend.Validations.DTO;

import com.skillvault.backend.dtos.Requests.CertificateRequestDTO;
import com.skillvault.backend.dtos.Requests.LoginUserDTO;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

@Component
public class DTOValidator {
    public static List<String> validateLogin(LoginUserDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto.email() == null || dto.email().isEmpty()) errors.add("Email is required.");
        if (dto.password() == null || dto.password().isEmpty()) errors.add("Password is required.");

        return errors;
    }

    public static List<String> validateSkillRequest(SkillRequestDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto.name() == null || dto.name().isEmpty()) errors.add("Skill name is required.");
        if (dto.description() == null || dto.description().isEmpty()) errors.add("Skill description is required.");

        return errors;
    }

    public static List<String> validateUserRequest(UserRequestDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto.name() == null || dto.name().isEmpty()) errors.add("Name is required.");
        if (dto.email() == null || dto.email().isEmpty()) errors.add("Email is required.");
        if (dto.password() == null || dto.password().isEmpty()) errors.add("Password is required.");
        if (dto.username() == null || dto.username().isEmpty()) errors.add("Username is required.");
        return errors;
    }

    public static List<String> validateCertificateRequestDTO(CertificateRequestDTO dto){
        List<String> errors = new ArrayList<>();

        if (dto.name() == null || dto.name().isEmpty()) errors.add("Name is required.");
        if (dto.skills() == null || dto.skills().isEmpty()) errors.add("List of skills is required.");
        return errors;
    }
}

package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Domain.UserProfilePicture;
import com.skillvault.backend.Repositories.SkillRepository;
import com.skillvault.backend.Repositories.UserProfilePictureRepository;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Requests.LoginUserDTO;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Requests.UpdateUserDTO;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import com.skillvault.backend.dtos.Responses.UserResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public User registerUser(UserRequestDTO dto, UserRole role){
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            log.warn("User's email {} already exists", dto.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An user with email: " + dto.email() + " already exists");
        }
        if (userRepository.findByUsername(dto.username()).isPresent()){
            log.warn("User's username {} already exists", dto.username());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An user with username: " + dto.username() + " already exists");
        }

        User user = new User(dto, role);
        String encodedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        return user;
    }

    public User loginUser(LoginUserDTO dto){

        User user = userRepository.findByEmail(dto.email()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var usernamePassword = new UsernamePasswordAuthenticationToken(user.getUsername(), dto.password());
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(usernamePassword);
        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", dto.email());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password or company is disabled");
        }

        return (User) authentication.getPrincipal();
    }

    @Transactional
    public User updateUser(UpdateUserDTO dto) {
        User user = tokenService.getLoggedEntity();
        if (user == null) throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "To access this area, you must be authenticated");

        if (dto == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing user data for update");


        if (dto.name() != null && !dto.name().isBlank()) user.setName(dto.name());
        if (dto.email() != null && !dto.email().isBlank()) user.setEmail(dto.email());
        if (dto.biography() != null) user.setBiography(dto.biography());
        if (dto.linkedin() != null) user.setLinkedin(dto.linkedin());
        if (dto.instagram() != null) user.setInstagram(dto.instagram());
        if (dto.github() != null) user.setGithub(dto.github());
        if (dto.site() != null) user.setSite(dto.site());


        return userRepository.save(user);
    }

}

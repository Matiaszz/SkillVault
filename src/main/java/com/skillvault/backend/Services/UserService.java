package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Requests.LoginUserDTO;
import com.skillvault.backend.dtos.Requests.UpdateUserDTO;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

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
                    HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        return (User) authentication.getPrincipal();
    }

    public User getUserById(UUID id){
        return userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User updateUser(UpdateUserDTO dto) {
        User user = tokenService.getLoggedEntity();
        if (user == null) throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "To access this area, you must be authenticated");

        if (dto == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing user data for update");

        user.updateFromDTO(dto);

        return userRepository.save(user);
    }

}

package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserRequestDTO dto){
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            log.warn("User's email {} already exists", dto.email());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An user with email: " + dto.email() + " already exists");
        }
        if (userRepository.findByUsername(dto.username()).isPresent()){
            log.warn("User's username {} already exists", dto.username());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An user with username: " + dto.username() + " already exists");
        }

        User user = new User(dto, UserRole.USER);
        String encodedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        return user;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow();
    }
}

package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserRequestDTO dto){
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

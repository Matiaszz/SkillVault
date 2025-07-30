package com.skillvault.backend.Utils;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Data
public class TestUtils {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;


    public User authenticateTest(UserRole role, String username, String email){
        User user = new User();
        user.setUsername(username);
        user.setName("UserName");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setRole(role);

        userRepository.saveAndFlush(user);


        var usernamePassword = new UsernamePasswordAuthenticationToken(user.getUsername(), "Password@123");
        var authentication = authenticationManager.authenticate(usernamePassword);


        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}

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


    public User authenticateTest(){
        this.userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setName("adminName");
        admin.setEmail("admin@emial.com");
        admin.setPassword(passwordEncoder.encode("Password@123"));
        admin.setRole(UserRole.USER);

        userRepository.save(admin);
        userRepository.flush();


        var usernamePassword = new UsernamePasswordAuthenticationToken(admin.getUsername(), "Password@123");
        var authentication = authenticationManager.authenticate(usernamePassword);


        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}

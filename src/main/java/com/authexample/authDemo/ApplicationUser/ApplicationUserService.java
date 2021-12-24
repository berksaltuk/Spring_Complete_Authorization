package com.authexample.authDemo.ApplicationUser;

import com.authexample.authDemo.Registration.token.ConfirmationToken;
import com.authexample.authDemo.Registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ApplicationUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND
            = "User with email %s not found";
    private final ApplicationUserRepository applicationUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return applicationUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND, email)));
    }

    public String signUpUser( ApplicationUser applicationUser)
    {
        boolean exists = applicationUserRepository.findByEmail(applicationUser.getEmail())
                .isPresent();

        if(exists)
        {
            throw new IllegalStateException("Email is in already use.");
        }

        String encodedPwd = bCryptPasswordEncoder.encode(applicationUser.getPassword());

        applicationUser.setPassword(encodedPwd);

        applicationUserRepository.save(applicationUser);

        String token = UUID.randomUUID().toString();



        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30),
                applicationUser
                );

        confirmationTokenService.saveConfirmationToken(confirmationToken);


        return token;
    }

    public int enableApplicationUser(String email)
    {
        return applicationUserRepository.enableApplicationUser(email);
    }
}

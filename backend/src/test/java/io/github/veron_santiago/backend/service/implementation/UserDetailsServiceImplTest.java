package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthRequest;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthResponse;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private ICompanyRepository companyRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserDetailsServiceImpl service;

    private final String name = "empresa1";
    private final String email = "empresa1@mail.com";
    private final String rawPassword = "pass123";
    private final String encodedPassword = "encodedPass";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loadUserByUsername_success() {
        Company company = Company.builder()
                .companyName(name)
                .password(encodedPassword)
                .build();
        when(companyRepository.findByCompanyNameOrEmail(name))
                .thenReturn(Optional.of(company));

        UserDetails userDetails = service.loadUserByUsername(name);

        assertEquals(name, userDetails.getUsername());
        assertEquals(encodedPassword, userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_notFound() {
        when(companyRepository.findByCompanyNameOrEmail(name))
                .thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(name)
        );
        assertTrue(ex.getMessage().contains("Compañia no encontrada"));
    }

    @Test
    void loginUser_companyNotFound() {
        AuthRequest req = new AuthRequest(name, rawPassword, null);
        when(companyRepository.findByCompanyNameOrEmail(name))
                .thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(
                ObjectNotFoundException.class,
                () -> service.loginUser(req)
        );
        assertEquals(ErrorMessages.COMPANY_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void loginUser_notVerified() {
        AuthRequest req = new AuthRequest(name, rawPassword, true);
        Company company = Company.builder()
                .companyName(name)
                .password(encodedPassword)
                .verified(false)
                .build();
        when(companyRepository.findByCompanyNameOrEmail(name))
                .thenReturn(Optional.of(company));

        AuthResponse res = service.loginUser(req);

        assertFalse(res.status());
        assertEquals(name, res.companyName());
        assertEquals("La compañia no está verificada. Compruebe su correo", res.message());
    }

    @Test
    void loginUser_badPassword() {
        AuthRequest req = new AuthRequest(name, rawPassword, false);
        Company company = Company.builder()
                .companyName(name)
                .password(encodedPassword)
                .verified(true)
                .build();
        when(companyRepository.findByCompanyNameOrEmail(name))
                .thenReturn(Optional.of(company));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> service.loginUser(req)
        );
        assertEquals("Contraseña invalida", ex.getMessage());
    }

    @Test
    void loginUser_success() {
        AuthRequest req = new AuthRequest(name, rawPassword, true);
        Company company = Company.builder()
                .companyName(name)
                .password(encodedPassword)
                .verified(true)
                .build();
        when(companyRepository.findByCompanyNameOrEmail(name))
                .thenReturn(Optional.of(company));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        String token = "tokenJwt";
        when(jwtUtil.createToken(any(Authentication.class), eq(true)))
                .thenReturn(token);

        AuthResponse res = service.loginUser(req);

        assertTrue(res.status());
        assertEquals(name, res.companyName());
        assertEquals(token, res.jwt());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, auth);
        assertEquals(name, auth.getPrincipal());
        assertEquals(encodedPassword, auth.getCredentials());
    }
}
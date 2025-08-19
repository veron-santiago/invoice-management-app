package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.configuration.security.CorsProperties;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthCreateCompany;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthRequest;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthResponse;
import io.github.veron_santiago.backend.service.implementation.UserDetailsServiceImpl;
import io.github.veron_santiago.backend.service.interfaces.ICompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/auth")
public class  AuthenticationController {

    private final UserDetailsServiceImpl userDetailsService;
    private final ICompanyService companyService;
    private final CorsProperties corsProperties;

    public AuthenticationController(UserDetailsServiceImpl userDetailsService, ICompanyService companyService, CorsProperties corsProperties) {
        this.userDetailsService = userDetailsService;
        this.companyService = companyService;
        this.corsProperties = corsProperties;
    }

    @PostMapping("/log-in")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest) {
        AuthResponse response = userDetailsService.loginUser(authRequest);
        if (response.status()) return new ResponseEntity<>(response, HttpStatus.OK);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthCreateCompany authCreateCompany){
        AuthResponse response = companyService.createCompany(authCreateCompany);
        if (response.status()) return new ResponseEntity<>(response, HttpStatus.CREATED);
        else return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        boolean verified = companyService.verifyEmail(token);
        String message = verified ? "Correo verificado con éxito" : "No se pudo verificar el correo";
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create( corsProperties.getAllowedOrigins().getFirst() + "/login?message=" + encodedMessage + "&verified=" + verified))
                .build();
    }


}

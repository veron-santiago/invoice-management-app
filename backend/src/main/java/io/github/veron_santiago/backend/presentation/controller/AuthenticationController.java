package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.presentation.dto.AuthCreateCompany;
import io.github.veron_santiago.backend.presentation.dto.AuthRequest;
import io.github.veron_santiago.backend.presentation.dto.AuthResponse;
import io.github.veron_santiago.backend.service.implementation.UserDetailsServiceImpl;
import io.github.veron_santiago.backend.service.interfaces.ICompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class  AuthenticationController {

    private final UserDetailsServiceImpl userDetailsService;
    private final ICompanyService companyService;

    public AuthenticationController(UserDetailsServiceImpl userDetailsService, ICompanyService companyService) {
        this.userDetailsService = userDetailsService;
        this.companyService = companyService;
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
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token){
        boolean verified = companyService.verifyEmail(token);
        if (verified) return ResponseEntity.ok("El correo electrónico se ha verificado correctamente");
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ha ocurrido un error al verificar el correo electrónico");
    }

}

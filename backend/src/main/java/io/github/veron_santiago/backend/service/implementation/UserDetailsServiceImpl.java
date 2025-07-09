package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.AuthRequest;
import io.github.veron_santiago.backend.presentation.dto.AuthResponse;
import io.github.veron_santiago.backend.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JwtUtil jwtUtil;
    private final ICompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDetailsServiceImpl(JwtUtil jwtUtil, ICompanyRepository companyRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String companyName) throws UsernameNotFoundException {
        Company company = companyRepository.findByCompanyNameOrEmail(companyName)
                .orElseThrow( () -> new UsernameNotFoundException("Compañia no encontrada con el nombre o email: " + companyName));

        return User.builder()
                .username(company.getCompanyName())
                .password(company.getPassword())
                .build();
    }

    private Authentication authenticate(String companyName, String password){
        UserDetails userDetails = this.loadUserByUsername(companyName);

        if(userDetails == null){
            throw new BadCredentialsException("Nombre de la compañia o contraseña invalida");
        }
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Contraseña invalida");
        }
        return new UsernamePasswordAuthenticationToken(companyName, userDetails.getPassword());
    }

    public AuthResponse loginUser(AuthRequest authRequest){
        String companyName = authRequest.companyName();
        String password = authRequest.password();
        boolean rememberMe = Boolean.TRUE.equals(authRequest.stayLogged());

        Company company = companyRepository.findByCompanyNameOrEmail(companyName)
                .orElseThrow(() -> new RuntimeException("Compañia no encontrada"));

        if (!company.isVerified()) return new AuthResponse(companyName, "La compañia no está verificada. Compruebe su correo", null, false);

        Authentication authentication = this.authenticate(companyName, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtil.createToken(authentication, rememberMe);
        return new AuthResponse(companyName, "Inicio de sesión exitoso", token, true);
    }

}
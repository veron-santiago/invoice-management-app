package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.AuthCreateCompany;
import io.github.veron_santiago.backend.presentation.dto.AuthResponse;
import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.service.interfaces.ICompanyService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.JwtUtil;
import io.github.veron_santiago.backend.util.mapper.CompanyMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompanyServiceImpl implements ICompanyService {

    private final ICompanyRepository companyRepository;
    private final JavaMailSender javaMailSender;
    private final CompanyMapper companyMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;

    public CompanyServiceImpl(ICompanyRepository companyRepository, JavaMailSender javaMailSender, CompanyMapper companyMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, AuthUtil authUtil) {
        this.companyRepository = companyRepository;
        this.javaMailSender = javaMailSender;
        this.companyMapper = companyMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authUtil = authUtil;
    }

    @Override
    public AuthResponse createCompany(AuthCreateCompany authCreateCompany) {
        String companyName = authCreateCompany.companyName();
        String password = authCreateCompany.password();
        String email = authCreateCompany.email();

        if (companyRepository.existsByCompanyName(companyName)) {
            return new AuthResponse(companyName, "Ya existe una compañia registrada con ese nombre.", null, false);
        }
        if (companyRepository.existsByEmail(email)){
            return new AuthResponse(email, "Ya existe una compañia registrada con ese correo.", null, false);
        }

        companyRepository.save(
                Company.builder()
                        .companyName(companyName)
                        .password(passwordEncoder.encode(password))
                        .email(email)
                        .build()
        );

        String token = jwtUtil.generateVerificationToken(email);
        sendVerificationEmail(email, token);
        return new AuthResponse(companyName, "Compañia registrada correctamente. Verifique el correo.", null, true);
    }

    @Override
    public CompanyDTO getCompany(HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        Company company = companyRepository.findById(companyId)
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage()));
        return companyMapper.companyToCompanyDTO(company, new CompanyDTO());
    }

    @Override
    public void deleteCompany(HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        if (companyRepository.existsById(companyId)) companyRepository.deleteById(companyId);
        else throw new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage());
    }

    @Override
    public boolean verifyEmail(String token) {
        String email = jwtUtil.extractCompanyNameFromToken(token);

        if (email == null) return false;

        Optional<Company> optionalCompany = companyRepository.findByEmail(email);

        if (optionalCompany.isEmpty()) return false;

        Company company = optionalCompany.get();

        if (company.isVerified()) return false;

        company.setVerified(true);
        companyRepository.save(company);
        return true;
    }

    private void sendVerificationEmail(String email, String verificationToken){
        String verificationUrl = "http://localhost:8080/auth/verify?token=" + verificationToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verificación de Correo Electrónico");
        message.setText("Haz click en el siguiente enlace para verificar tu correo: " + verificationUrl);
        javaMailSender.send(message);
    }


}

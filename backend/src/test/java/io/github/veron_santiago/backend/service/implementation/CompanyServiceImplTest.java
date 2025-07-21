package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthCreateCompany;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthResponse;
import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.JwtUtil;
import io.github.veron_santiago.backend.util.mapper.CompanyMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceImplTest {

    @InjectMocks private CompanyServiceImpl service;
    @Mock private ICompanyRepository companyRepository;
    @Mock private JavaMailSender javaMailSender;
    @Mock private CompanyMapper companyMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthUtil authUtil;
    @Mock private HttpServletRequest request;

    private AuthCreateCompany createDto;
    private final String name = "empresa1";
    private final String email = "correo@mail.com";
    private final String rawPassword = "pass123";
    private final String token = "token123";

    @BeforeEach
    void setUp() {
        createDto = new AuthCreateCompany(name, rawPassword, email);
    }

    @Test
    void createCompany_successful() {
        when(companyRepository.existsByCompanyName(name)).thenReturn(false);
        when(companyRepository.existsByEmail(email)).thenReturn(false);
        String encodedPassword = "encodedPass";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(jwtUtil.generateVerificationToken(email)).thenReturn(token);

        AuthResponse response = service.createCompany(createDto);

        assertTrue(response.status());
        assertEquals(name, response.companyName());
        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(captor.capture());
        assertEquals(encodedPassword, captor.getValue().getPassword());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        assertEquals("Compañia registrada correctamente. Verifique el correo.", response.message());
    }

    @Test
    void createCompany_nameExists() {
        when(companyRepository.existsByCompanyName(name)).thenReturn(true);

        AuthResponse response = service.createCompany(createDto);

        assertFalse(response.status());
        assertEquals(name, response.companyName());
        assertEquals("Ya existe una compañia registrada con ese nombre.", response.message());
        verify(companyRepository, never()).save(any());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void createCompany_emailExists() {
        when(companyRepository.existsByCompanyName(name)).thenReturn(false);
        when(companyRepository.existsByEmail(email)).thenReturn(true);

        AuthResponse response = service.createCompany(createDto);

        assertFalse(response.status());
        assertEquals(email, response.companyName());
        assertEquals("Ya existe una compañia registrada con ese correo.", response.message());
        verify(companyRepository, never()).save(any());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void getCompany_successful() {
        Long id = 1L;
        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(id);
        Company company = Company.builder().id(id).companyName(name).email(email).build();
        when(companyRepository.findById(id)).thenReturn(Optional.of(company));
        CompanyDTO dto = new CompanyDTO();
        when(companyMapper.companyToCompanyDTO(eq(company), any(CompanyDTO.class))).thenReturn(dto);

        CompanyDTO result = service.getCompany(request);

        assertSame(dto, result);
    }

    @Test
    void getCompany_notFound() {
        Long id = 2L;
        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(id);
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> service.getCompany(request));
        assertEquals(ErrorMessages.COMPANY_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void deleteCompany_successful() {
        Long id = 3L;
        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(id);
        when(companyRepository.existsById(id)).thenReturn(true);

        service.deleteCompany(request);

        verify(companyRepository).deleteById(id);
    }

    @Test
    void deleteCompany_notFound() {
        Long id = 4L;
        when(authUtil.getAuthenticatedCompanyId(request)).thenReturn(id);
        when(companyRepository.existsById(id)).thenReturn(false);

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> service.deleteCompany(request));
        assertEquals(ErrorMessages.COMPANY_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void verifyEmail_tokenInvalid() {
        when(jwtUtil.extractCompanyNameFromToken(token)).thenReturn(null);

        boolean result = service.verifyEmail(token);

        assertFalse(result);
    }

    @Test
    void verifyEmail_emailNotFound() {
        when(jwtUtil.extractCompanyNameFromToken(token)).thenReturn(name);
        when(companyRepository.findByEmail(name)).thenReturn(Optional.empty());

        assertFalse(service.verifyEmail(token));
    }

    @Test
    void verifyEmail_alreadyVerified() {
        Company company = Company.builder().id(5L).companyName(name).email(email).verified(true).build();
        when(jwtUtil.extractCompanyNameFromToken(token)).thenReturn(name);
        when(companyRepository.findByEmail(name)).thenReturn(Optional.of(company));

        assertFalse(service.verifyEmail(token));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void verifyEmail_successful() {
        Company company = Company.builder().id(6L).companyName(name).email(email).verified(false).build();
        when(jwtUtil.extractCompanyNameFromToken(token)).thenReturn(name);
        when(companyRepository.findByEmail(name)).thenReturn(Optional.of(company));

        boolean result = service.verifyEmail(token);

        assertTrue(result);
        assertTrue(company.isVerified());
        verify(companyRepository).save(company);
    }
}

package io.github.veron_santiago.backend.service.implementation;

import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthCreateCompany;
import io.github.veron_santiago.backend.presentation.dto.auth.AuthResponse;
import io.github.veron_santiago.backend.presentation.dto.response.CompanyDTO;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateAddress;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateEmail;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdateName;
import io.github.veron_santiago.backend.presentation.dto.update.CompanyUpdatePassword;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.InvalidFieldException;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import io.github.veron_santiago.backend.service.exception.ResourceConflictException;
import io.github.veron_santiago.backend.service.interfaces.ICompanyService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.JwtUtil;
import io.github.veron_santiago.backend.util.mapper.CompanyMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompanyServiceImpl implements ICompanyService {

    @Value("${app.api.uri}") private String apiUri;

    private final ICompanyRepository companyRepository;
    private final JavaMailSender javaMailSender;
    private final CompanyMapper companyMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;
    private final CloudinaryService cloudinaryService;

    public CompanyServiceImpl(ICompanyRepository companyRepository, JavaMailSender javaMailSender, CompanyMapper companyMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, AuthUtil authUtil, CloudinaryService cloudinaryService) {
        this.companyRepository = companyRepository;
        this.javaMailSender = javaMailSender;
        this.companyMapper = companyMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authUtil = authUtil;
        this.cloudinaryService = cloudinaryService;
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

        Company company = companyRepository.save(
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
        Company company = authUtil.getCompanyByRequest(request);
        return companyMapper.companyToCompanyDTO(company, new CompanyDTO());
    }

    @Override
    public CompanyDTO updateAddress(CompanyUpdateAddress companyUpdateAddress, HttpServletRequest request) {
        Company company = authUtil.getCompanyByRequest(request);
        String address = companyUpdateAddress.address();
        if (Objects.equals(company.getAddress(), address)) throw new InvalidFieldException(ErrorMessages.EMAIL_SAME_AS_CURRENT.getMessage());
        company.setAddress(address);
        Company savedCompany = companyRepository.save(company);
        return companyMapper.companyToCompanyDTO(savedCompany, new CompanyDTO());
    }

    @Override
    public CompanyDTO updateName(CompanyUpdateName companyUpdateName, HttpServletRequest request) {
        Company company = authUtil.getCompanyByRequest(request);
        String name = companyUpdateName.name();
        if (name == null || name.isEmpty()) throw new InvalidFieldException(ErrorMessages.EMPTY_FIELD.getMessage());

        Company comp = companyRepository.findByCompanyName(name).orElse(null);
        if (comp != null && !comp.equals(company)) throw new ResourceConflictException(ErrorMessages.CONFLICT_COMPANY_NAME.getMessage());

        company.setCompanyName(name);
        Company savedCompany = companyRepository.save(company);
        return companyMapper.companyToCompanyDTO(savedCompany, new CompanyDTO());
    }

    @Override
    public void updateEmail(CompanyUpdateEmail companyUpdateEmail, HttpServletRequest request) {
        Company company = authUtil.getCompanyByRequest(request);
        String currentEmail = company.getEmail();
        String newEmail = companyUpdateEmail.email().toLowerCase();

        if (newEmail.equalsIgnoreCase(currentEmail)) throw new InvalidFieldException(ErrorMessages.EMAIL_SAME_AS_CURRENT.getMessage());

        Company existingCompany = companyRepository.findByEmail(newEmail).orElse(null);
        if (existingCompany != null) throw new ResourceConflictException(ErrorMessages.EMAIL_ALREADY_IN_USE.getMessage());

        company.setEmail(newEmail);
        companyRepository.save(company);

        SimpleMailMessage notification = new SimpleMailMessage();
        notification.setTo(currentEmail);
        notification.setSubject("Tu email ha sido cambiado");
        notification.setText("Tu dirección de correo fue actualizada a: " + newEmail);
        javaMailSender.send(notification);
    }

    @Override
    public void updatePassword(CompanyUpdatePassword updatePassword, HttpServletRequest request) {
        Company company = authUtil.getCompanyByRequest(request);
        String actualP = updatePassword.actualPassword();
        String newP = updatePassword.newPassword();
        if (!passwordEncoder.matches(actualP, company.getPassword())) throw new BadCredentialsException(ErrorMessages.INCORRECT_CURRENT_PASSWORD.getMessage());
        if (passwordEncoder.matches(newP, company.getPassword())) throw new InvalidFieldException(ErrorMessages.PASSWORD_SAME_AS_CURRENT.getMessage());
        company.setPassword(passwordEncoder.encode(newP));
        companyRepository.save(company);
    }

    @Override
    public String getLogo(HttpServletRequest request) {
        Company company = authUtil.getCompanyByRequest(request);
        String logoPath = company.getLogoPath();
        return logoPath == null || logoPath.isBlank() ? null : logoPath;
    }

    @Override
    public void uploadLogo(MultipartFile file, HttpServletRequest request) throws IOException {
        String extension = Objects.requireNonNull(FilenameUtils.getExtension(file.getOriginalFilename())).toLowerCase();
        if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("jpeg")) {
            throw new IllegalArgumentException("Sólo se aceptan archivos PNG y JPG");
        }

        Company company = authUtil.getCompanyByRequest(request);
        Long companyId = company.getId();
        String url = "logos/" + companyId + "/";

        UUID uuid = UUID.randomUUID();
        String name = uuid.toString().substring(0, 13);

        String path = cloudinaryService.uploadField(file, url, name, false);
        company.setLogoPath(path);
        companyRepository.save(company);
    }

    @Override
    public void deleteCompany(HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        if (companyRepository.existsById(companyId)) companyRepository.deleteById(companyId);
        else throw new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage());
    }

    @Override
    public boolean verifyEmail(String token) {
        String email = jwtUtil.extractSubject(token);
        if (email == null) return false;

        Optional<Company> optionalCompany = companyRepository.findByEmail(email);

        if (optionalCompany.isEmpty()) return false;

        Company company = optionalCompany.get();

        if (company.isVerified()) return false;

        company.setVerified(true);
        companyRepository.save(company);
        return true;
    }

    @Override
    public boolean hasAccessToken(HttpServletRequest request) {
        Company company = authUtil.getCompanyByRequest(request);
        String accessToken = company.getMpAccessToken();
        return accessToken != null && !accessToken.isEmpty();
    }

    private void sendVerificationEmail(String email, String verificationToken){
        String verificationUrl = apiUri + "/auth/verify?token=" + verificationToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verificación de Correo Electrónico");
        message.setText("Haz click en el siguiente enlace para verificar tu correo: " + verificationUrl);
        javaMailSender.send(message);
    }

}

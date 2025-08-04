package io.github.veron_santiago.backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${security.jwt.private-key}")
    private String privateKey;

    @Value("${security.jwt.user.generator}")
    private String userGenerator;

    private static final long EXPIRATION_TIME_DEFAULT = 86400000L;
    private static final long EXPIRATION_TIME_REMEMBER_ME = 604800000L;

    private final ICompanyRepository companyRepository;

    public JwtUtil(ICompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public String createToken(Authentication authentication, boolean rememberMe){
        Algorithm algorithm = Algorithm.HMAC256(privateKey);
        String  companyName = authentication.getPrincipal().toString();
        Company company = companyRepository.findById(Long.valueOf(companyName))
                .orElseThrow( () -> new ObjectNotFoundException(ErrorMessages.COMPANY_NOT_FOUND.getMessage()));
        return JWT.create()
                .withIssuer(userGenerator)
                .withSubject(company.getId().toString())
                .withClaim("email", company.getEmail())
                .withClaim("companyName", company.getCompanyName())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + (rememberMe ? EXPIRATION_TIME_REMEMBER_ME : EXPIRATION_TIME_DEFAULT)))
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(privateKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(userGenerator)
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        } catch (JWTVerificationException e){
            throw new JWTVerificationException("Token inválido");
        }
    }

    public String extractCompanyName(DecodedJWT decodedJWT){
        return decodedJWT.getClaim("companyName").asString();
    }

    public String extractCompanyNameFromToken(String token){
        DecodedJWT decodedJWT = validateToken(token);
        return extractCompanyName(decodedJWT);
    }

    public Claim getSpecificClaim(DecodedJWT decodedJWT, String claimName){
        return decodedJWT.getClaim(claimName);
    }

    public Map<String, Claim> getAllClaims(DecodedJWT decodedJWT){
        return decodedJWT.getClaims();
    }

    public String extractEmailFromToken(String token) {
        DecodedJWT decodedJWT = validateToken(token);
        return decodedJWT.getClaim("email").asString();
    }

    public String extractSubject(String token){
        DecodedJWT decodedJWT = validateToken(token);
        return decodedJWT.getSubject();
    }

    public Long extractCompanyIdFromToken(String token){
        DecodedJWT decodedJWT = validateToken(token);
        return Long.valueOf(decodedJWT.getSubject());
    }

    public String generateVerificationToken(String email){
        Algorithm algorithm = Algorithm.HMAC256(privateKey);
        return JWT.create()
                .withIssuer(userGenerator)
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_DEFAULT))
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);
    }
}

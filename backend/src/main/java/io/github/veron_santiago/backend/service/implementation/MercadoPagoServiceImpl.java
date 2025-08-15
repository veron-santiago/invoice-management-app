package io.github.veron_santiago.backend.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.veron_santiago.backend.persistence.entity.Company;
import io.github.veron_santiago.backend.persistence.repository.ICompanyRepository;
import io.github.veron_santiago.backend.service.exception.BadGatewayException;
import io.github.veron_santiago.backend.service.exception.ErrorMessages;
import io.github.veron_santiago.backend.service.exception.InvalidFieldException;
import io.github.veron_santiago.backend.service.exception.UnprocessableEntity;
import io.github.veron_santiago.backend.service.interfaces.IMercadoPagoService;
import io.github.veron_santiago.backend.util.AuthUtil;
import io.github.veron_santiago.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class MercadoPagoServiceImpl implements IMercadoPagoService {

    @Value("${mp_client_id}") private String clientId;
    @Value("${mp_client_secret}") private String clientSecret;
    @Value("${app.mp.redirect-uri}") private String redirectUri;
    @Value("${security.jwt.private-key}") private String privateKey;

    private final ICompanyRepository companyRepository;
    private final RestTemplate restTemplate;
    private final AuthUtil authUtil;
    private final JwtUtil jwtUtil;

    public MercadoPagoServiceImpl(ICompanyRepository companyRepository, RestTemplate restTemplate, AuthUtil authUtil, JwtUtil jwtUtil) {
        this.companyRepository = companyRepository;
        this.restTemplate = restTemplate;
        this.authUtil = authUtil;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String buildAuthUrl(HttpServletRequest request) {
        Long companyId = authUtil.getAuthenticatedCompanyId(request);
        String state = jwtUtil.generateState(companyId, privateKey);
        return UriComponentsBuilder.fromUriString("https://auth.mercadopago.com.ar/authorization")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("platform_id", "mp")
                .queryParam("state", state)
                .queryParam("redirect_uri", redirectUri)
                .build()
                .toUriString();

        // https://auth.mercadopago.com/authorization
        // ?client_id=APP_ID
        // &response_type=code
        // &platform_id=mp
        // &state=RANDOM_ID
        // &redirect_uri=https://www.mercadopago.com.ar/developers/example/redirect-url
    }

    @Override
    public void exchangeCodeForTokens(String code, String state, HttpServletRequest request) {

        Long companyId = jwtUtil.validateState(state, privateKey);
        if (companyId == null) throw new InvalidFieldException("Parámetro state inválido");

        Company company = authUtil.getCompanyById(companyId);

        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity<>(form, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "https://api.mercadopago.com/oauth/token",
                httpEntity,
                JsonNode.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) throw new BadGatewayException("Fallo en la comunicación con Mercado Pago");

        JsonNode json = response.getBody();

        if (json == null
                || !json.has("access_token")
                || !json.has("refresh_token")
                || !json.has("expires_in")) throw new BadGatewayException("Fallo en la comunicación con Mercado Pago");

        String accessToken = json.get("access_token").asText();
        String refreshToken = json.get("refresh_token").asText();
        long expiresIn = json.get("expires_in").asLong();

        company.setMpAccessToken(accessToken);
        company.setMpRefreshToken(refreshToken);
        company.setMpTokenExpiration(Instant.now().plusSeconds(expiresIn).getEpochSecond());
        companyRepository.save(company);
    }

    @Override
    public String createPaymentLink(Company company, BigDecimal amount) {
        String accessToken = company.getMpAccessToken();
        if (accessToken == null || accessToken.isEmpty()) throw new UnprocessableEntity("La compañía no está vinculada a Mercado Pago");
        if (company.getMpTokenExpiration() < Instant.now().getEpochSecond() + 60) accessToken = refreshAccessToken(company);

        Map<String, Object> item = Map.of(
                "title", "Factuara de " + company.getCompanyName(),
                "quantity", 1,
                "unit_price", amount
        );

        Map<String, Object> preference = Map.of(
                "items", List.of(item)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(preference, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "https://api.mercadopago.com/checkout/preferences",
                entity,
                JsonNode.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) throw new BadGatewayException(ErrorMessages.QR.getMessage());
        JsonNode body = response.getBody();
        if (body == null || !body.has("init_point")) throw new BadGatewayException(ErrorMessages.QR.getMessage());
        return body.get("init_point").asText();
    }

    private String refreshAccessToken(Company company){
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", company.getMpRefreshToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "https://api.mercadopago.com/oauth/token",
                request,
                JsonNode.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) throw new BadGatewayException(ErrorMessages.QR.getMessage());
        JsonNode json = response.getBody();

        if (json == null ||
                !json.has("access_token") ||
                !json.has("refresh_token") ||
                !json.has("expires_in")) throw new BadGatewayException(ErrorMessages.QR.getMessage());


        String newAccessToken = json.get("access_token").asText();
        String newRefreshToken = json.get("refresh_token").asText();
        long expiresIn = json.get("expires_in").asLong();

        company.setMpAccessToken(newAccessToken);
        company.setMpRefreshToken(newRefreshToken);
        company.setMpTokenExpiration(Instant.now().plusSeconds(expiresIn).getEpochSecond());

        Company saved = companyRepository.save(company);
        return saved.getMpAccessToken();
    }


}

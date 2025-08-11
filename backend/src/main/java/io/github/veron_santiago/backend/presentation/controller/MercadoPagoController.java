package io.github.veron_santiago.backend.presentation.controller;

import io.github.veron_santiago.backend.service.interfaces.IMercadoPagoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/mp")
public class MercadoPagoController {

    private final IMercadoPagoService mercadoPagoService;

    public MercadoPagoController(IMercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @GetMapping("/connect")
    public ResponseEntity<String> connect(HttpServletRequest request) {
        String url = mercadoPagoService.buildAuthUrl(request);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/callback")
    public void handleCallback(@RequestParam("code") String code,
                                            @RequestParam("state") String state,
                                            HttpServletResponse response,
                                            HttpServletRequest request) throws IOException {
        mercadoPagoService.exchangeCodeForTokens(code, state, request);

        String html = """
        <html>
        <body>
            <script>
                if (window.opener) {
                    window.opener.postMessage({ mpLinked: true }, "*");
                }
                window.close();
            </script>
        </body>
        </html>
        """;

        response.setContentType("text/html");
        response.getWriter().write(html);

    }

}

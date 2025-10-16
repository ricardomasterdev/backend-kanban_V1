package com.seuprojeto.kanban.web;

import com.seuprojeto.kanban.dto.AuthDTOs.LoginRequest;
import com.seuprojeto.kanban.dto.AuthDTOs.TokenResponse;
import com.seuprojeto.kanban.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Geração de token JWT para acessar as rotas protegidas.")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager, JwtService jwt){
        this.authManager = authManager; this.jwt = jwt;
    }

    @Operation(summary = "Login (gera token)")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
            String token = jwt.generate(req.email());
            return ResponseEntity.ok(new TokenResponse(token, 28800));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("{\"message\":\"Credenciais inválidas\"}");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("{\"message\":\"Não autenticado\"}");
        }
    }
}

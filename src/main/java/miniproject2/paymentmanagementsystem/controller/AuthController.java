package miniproject2.paymentmanagementsystem.controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miniproject2.paymentmanagementsystem.dto.AuthResponseDTO;
import miniproject2.paymentmanagementsystem.dto.LoginRequestDTO;
import miniproject2.paymentmanagementsystem.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
@Slf4j

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        try {
            AuthResponseDTO response = authService.authenticate(loginRequest);
            log.info("Successful login for user: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getEmail(), e);
            throw e;
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        log.info("Logout request received");
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid authorization header in logout request");
            return ResponseEntity.badRequest().body("Invalid authorization header");
        }
        log.info("Successful logout");
        return ResponseEntity.ok("Successfully logged out. Please discard your token.");
    }
}
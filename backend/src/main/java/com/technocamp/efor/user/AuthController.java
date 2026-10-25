package com.technocamp.efor.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record MeResponse(String username, String agentId, String firstName, String surname, Role role) {
    }

    private final CurrentUserService currentUserService;

    public AuthController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    /** Giriş bilgilerini doğrular ve oturum açan kullanıcıyı döner. */
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        AppUser user = currentUserService.get(authentication);
        return new MeResponse(user.getUsername(), user.getAgentId(), user.getFirstName(), user.getSurname(),
                user.getRole());
    }
}

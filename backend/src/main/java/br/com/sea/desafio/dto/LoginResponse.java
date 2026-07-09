package br.com.sea.desafio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private final String token;
    private final String tipo;
    private final String username;
    private final String role;

    public static LoginResponse bearer(String token, String username, String role) {
        return new LoginResponse(token, "Bearer", username, role);
    }
}

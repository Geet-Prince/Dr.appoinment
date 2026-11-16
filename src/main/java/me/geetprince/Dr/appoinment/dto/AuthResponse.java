package me.geetprince.Dr.appoinment.dto;

import lombok.*;
import me.geetprince.Dr.appoinment.models.UserRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private Long userId;
    private String email;
    private UserRole role;
}

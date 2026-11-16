package me.geetprince.Dr.appoinment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import me.geetprince.Dr.appoinment.models.UserRole;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private UserRole role;
    private Boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}

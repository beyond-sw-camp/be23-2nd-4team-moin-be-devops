package com.team4.moin.user.dtos.passwordupdatedtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordUpdateDto {
    private String email;
    private String newPassword;
}

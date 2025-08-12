package com.expensetracker.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateRequest {
    private String oldPassword;
    private String newPassword;
}

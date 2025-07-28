package com.expensetracker.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    public String oldPassword;
    public String newPassword;
}

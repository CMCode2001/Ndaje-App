package com.ndaje.trip.dto.response;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String email;
    private String role; // "PASSENGER" or "DRIVER"
}

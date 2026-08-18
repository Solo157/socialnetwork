package com.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;

/**
 * DTO для возврата информации по пользователю.
 */
@Data
@Builder
public class UserResponse {

    private String id;
    private String firstName;
    private String secondName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate birthdate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String biography;
    private String city;

}

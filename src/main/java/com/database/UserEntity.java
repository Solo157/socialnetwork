package com.database;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.util.*;

/**
 * Сущность, хранящая в себе данные пользователя.
 */
@FieldNameConstants
@Data
@Builder
public class UserEntity {

    private UUID id;
    private String firstName;
    private String secondName;
    private LocalDate birthdate;
    private String biography;
    private String city;
    private String passwordHash;

}

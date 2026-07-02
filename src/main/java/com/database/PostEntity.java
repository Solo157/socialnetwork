package com.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class PostEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String text;
    /**
     * Фактически это идентификатор пользователя.
     */
    private String authorId;
    private LocalDateTime createdAt;

}

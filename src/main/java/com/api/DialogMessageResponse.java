package com.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DialogMessageResponse {

    private String id;
    private String dialogId;
    private String senderId;
    private String receiverId;
    private String text;
    private LocalDateTime createdAt;

}

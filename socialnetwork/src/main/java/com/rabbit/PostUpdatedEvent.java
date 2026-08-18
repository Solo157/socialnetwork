package com.rabbit;

import com.database.PostEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostUpdatedEvent {

    private String authorId;
    private PostEntity post;

    public PostUpdatedEvent(String authorId, PostEntity post) {
        this.authorId = authorId;
        this.post = post;
    }
}

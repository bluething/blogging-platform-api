package io.github.bluething.playground.java.bloggingplatformapi.domain;

import java.time.Instant;
import java.util.List;

public record PostData(String id,
                       String title,
                       String content,
                       CategoryData category,
                       List<TagData> tags,
                       Instant createdAt,
                       Instant updatedAt) {
}

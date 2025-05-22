package io.github.bluething.playground.java.bloggingplatformapi.rest;

import java.time.Instant;
import java.util.List;

record PostResponse(String id,
    String title,
    String content,
    CategoryResponse category,
    List<TagResponse> tags,
    Instant createdAt,
    Instant updatedAt) {
}

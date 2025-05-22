package io.github.bluething.playground.java.bloggingplatformapi.domain;

import java.util.List;

public record CreatePostCommand(String title,
                                String content,
                                String categoryId,
                                List<String> tagIds) {
}

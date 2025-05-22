package io.github.bluething.playground.java.bloggingplatformapi.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

record PostRequest(
        @NotBlank(message = "Title must not be blank") String title,
        @NotBlank(message = "Content must not be blank") String content,
        @NotBlank(message = "Category ID must not be blank") String categoryId,
        @NotEmpty(message = "At least one tag ID is required") List<@NotBlank(message = "Tag ID must not be blank") String> tagIds
) {
}

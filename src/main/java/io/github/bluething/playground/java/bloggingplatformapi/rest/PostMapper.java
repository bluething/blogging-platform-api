package io.github.bluething.playground.java.bloggingplatformapi.rest;

import io.github.bluething.playground.java.bloggingplatformapi.domain.CreatePostCommand;
import io.github.bluething.playground.java.bloggingplatformapi.domain.PostData;
import io.github.bluething.playground.java.bloggingplatformapi.domain.UpdatePostCommand;

import java.util.List;

public class PostMapper {
    public static CreatePostCommand toCreatePostCommand(PostRequest postRequest) {
        return new CreatePostCommand(
                postRequest.title(),
                postRequest.content(),
                postRequest.categoryId(),
                postRequest.tagIds()
        );
    }
    public static UpdatePostCommand toUpdateCommand(PostRequest postRequest) {
        return new UpdatePostCommand(
                postRequest.title(),
                postRequest.content(),
                postRequest.categoryId(),
                postRequest.tagIds()
        );
    }

    public static PostResponse toResponse(PostData postData) {
        CategoryResponse category = new CategoryResponse(
                postData.category().id(),
                postData.category().name()
        );
        List<TagResponse> tags = postData.tags().stream()
                .map(tag -> new TagResponse(tag.id(), tag.name()))
                .toList();
        return new PostResponse(
                postData.id(),
                postData.title(),
                postData.content(),
                category,
                tags,
                postData.createdAt(),
                postData.updatedAt()
        );
    }
}

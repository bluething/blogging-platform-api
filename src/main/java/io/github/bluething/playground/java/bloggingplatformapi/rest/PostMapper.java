package io.github.bluething.playground.java.bloggingplatformapi.rest;

import io.github.bluething.playground.java.bloggingplatformapi.domain.*;

import java.util.List;

class PostMapper {

    /**
     * Map API request to service-layer command for creating a post.
     */
    public static CreatePostCommand toCreatePostCommand(PostRequest postRequest) {
        return new CreatePostCommand(
                postRequest.title(),
                postRequest.content(),
                postRequest.categoryId(),
                postRequest.tagIds()
        );
    }

    /**
     * Map API request to service-layer command for updating a post.
     */
    public static UpdatePostCommand toUpdateCommand(PostRequest postRequest) {
        return new UpdatePostCommand(
                postRequest.title(),
                postRequest.content(),
                postRequest.categoryId(),
                postRequest.tagIds()
        );
    }

    /**
     * Map service-layer data to API response DTO.
     */
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

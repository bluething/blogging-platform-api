package io.github.bluething.playground.java.bloggingplatformapi.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bluething.playground.java.bloggingplatformapi.domain.CategoryData;
import io.github.bluething.playground.java.bloggingplatformapi.domain.PostData;
import io.github.bluething.playground.java.bloggingplatformapi.domain.PostService;
import io.github.bluething.playground.java.bloggingplatformapi.domain.TagData;
import io.github.bluething.playground.java.bloggingplatformapi.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PostService postService;

    private static final String BASE_URL = "/api/v1/posts";

    private PostData samplePostData() {
        return new PostData(
                "01F8MECHZX3TBDSZ7XRADM79XE",
                "Sample Title",
                "Sample Content",
                new CategoryData("cat1", "Tech"),
                List.of(new TagData("tag1", "Java"), new TagData("tag2", "Spring")),
                Instant.parse("2021-09-01T12:00:00Z"),
                Instant.parse("2021-09-01T12:00:00Z")
        );
    }

    @Test
    @DisplayName("POST /api/v1/posts - Success")
    void testCreatePost() throws Exception {
        var request = new PostRequest(
                "New Title",
                "New Content",
                "cat1",
                List.of("tag1", "tag2")
        );
        var command = PostMapper.toCreatePostCommand(request);
        var createdData = samplePostData();
        given(postService.createPost(ArgumentMatchers.eq(command))).willReturn(createdData);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BASE_URL + "/" + createdData.id()))
                .andExpect(jsonPath("$.id").value(createdData.id()));
    }


    @Test
    @DisplayName("GET /api/v1/posts/{id} - Found")
    void testGetPostFound() throws Exception {
        var data = samplePostData();
        given(postService.getPostById(data.id())).willReturn(Optional.of(data));

        mockMvc.perform(get(BASE_URL + "/{id}", data.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(data.id()))
                .andExpect(jsonPath("$.title").value(data.title()))
                .andExpect(jsonPath("$.category.id").value(data.category().id()))
                .andExpect(jsonPath("$.tags[0].id").value("tag1"));
    }

    @Test
    @DisplayName("GET /api/v1/posts?term=search - Search")
    void testSearchPosts() throws Exception {
        var data = samplePostData();
        given(postService.searchPosts("sample")).willReturn(List.of(data));

        mockMvc.perform(get(BASE_URL).param("term", "sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].title").value(data.title()));
    }

    @Test
    @DisplayName("PUT /api/v1/posts/{id} - Success")
    void testUpdatePost() throws Exception {
        var request = new PostRequest(
                "Updated Title",
                "Updated Content",
                "cat1",
                List.of("tag1")
        );
        var command = PostMapper.toUpdateCommand(request);
        var updatedData = samplePostData();
        given(postService.updatePost(ArgumentMatchers.eq(updatedData.id()), ArgumentMatchers.eq(command)))
                .willReturn(updatedData);

        mockMvc.perform(put(BASE_URL + "/{id}", updatedData.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(updatedData.title()));
    }

    @Test
    @DisplayName("DELETE /api/v1/posts/{id} - Success")
    void testDeletePost() throws Exception {
        doNothing().when(postService).deletePost("toDelete");

        mockMvc.perform(delete(BASE_URL + "/toDelete"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/posts/{id} - Not Found")
    void testDeletePostNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Post", "missing")).when(postService).deletePost("missing");

        mockMvc.perform(delete(BASE_URL + "/missing"))
                .andExpect(status().isNotFound());
    }
}
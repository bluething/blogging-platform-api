package io.github.bluething.playground.java.bloggingplatformapi.rest;

import io.github.bluething.playground.java.bloggingplatformapi.domain.PostService;
import io.github.bluething.playground.java.bloggingplatformapi.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@Validated
@RequiredArgsConstructor
class PostController {

    private final PostService postService;

    @PostMapping
    ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest) {
        var command = PostMapper.toCreatePostCommand(postRequest);
        var data = postService.createPost(command);
        var response = PostMapper.toResponse(data);
        return ResponseEntity.created(URI.create("/api/v1/posts/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    ResponseEntity<PostResponse> getPostById(@PathVariable("id") String id) {
        var data = postService.getPostById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return ResponseEntity.ok(PostMapper.toResponse(data));
    }

    @GetMapping
    ResponseEntity<List<PostResponse>> getPosts(@RequestParam(value = "term", required = false) String term) {
        var results = (term == null || term.isBlank())
                ? postService.getAllPosts()
                : postService.searchPosts(term);
        var responses = results.stream()
                .map(PostMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    ResponseEntity<PostResponse> updatePost(@PathVariable("id") String id, @Valid @RequestBody PostRequest postRequest) {
        var command = PostMapper.toUpdateCommand(postRequest);
        var data = postService.updatePost(id, command);
        var response = PostMapper.toResponse(data);
        return ResponseEntity.ok(response);
    }
}

package io.github.bluething.playground.java.bloggingplatformapi.domain;

import java.util.List;
import java.util.Optional;

public interface PostService {
    PostData createPost(CreatePostCommand command);
    PostData updatePost(String id, UpdatePostCommand command);
    void deletePost(String id);
    Optional<PostData> getPostById(String id);
    List<PostData> getAllPosts();
    List<PostData> searchPosts(String term);
}

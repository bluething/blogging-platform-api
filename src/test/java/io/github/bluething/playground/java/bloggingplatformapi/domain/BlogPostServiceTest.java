package io.github.bluething.playground.java.bloggingplatformapi.domain;

import com.github.f4b6a3.ulid.UlidCreator;
import io.github.bluething.playground.java.bloggingplatformapi.exception.ResourceNotFoundException;
import io.github.bluething.playground.java.bloggingplatformapi.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        BlogPostService.class,
        BlogPostServiceTest.TestCacheConfig.class
})
class BlogPostServiceTest {
    @TestConfiguration
    @EnableCaching
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("posts");
        }
    }

    @MockitoBean
    private PostRepository postRepository;
    @MockitoBean
    private CategoryRepository categoryRepository;
    @MockitoBean
    private TagRepository tagRepository;

    @Autowired
    private PostService postService;

    @Autowired
    CacheManager cacheManager;

    private CategoryEntity category;
    private TagEntity tag1;
    private TagEntity tag2;

    @BeforeEach
    void setup() {
        category = new CategoryEntity(UlidCreator.getUlid().toString(), "Tech");
        tag1 = new TagEntity(UlidCreator.getUlid().toString(), "Java");
        tag2 = new TagEntity(UlidCreator.getUlid().toString(), "Spring");
        cacheManager.getCache("posts").clear();
    }

    @Test
    @DisplayName("createPost should persist and return PostData")
    void testCreatePostSuccess() {
        // given
        CreatePostCommand cmd = new CreatePostCommand(
                "Title", "Content", category.getId(), List.of(tag1.getId(), tag2.getId())
        );
        given(categoryRepository.findById(category.getId()))
                .willReturn(Optional.of(category));
        given(tagRepository.findAllById(cmd.tagIds()))
                .willReturn(List.of(tag1, tag2));

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        PostEntity savedEntity = new PostEntity(
                "xyz", cmd.title(), cmd.content(), category,
                Set.of(tag1, tag2), Instant.now(), Instant.now()
        );
        given(postRepository.save(any(PostEntity.class))).willReturn(savedEntity);

        // when
        PostData result = postService.createPost(cmd);

        // then
        then(postRepository).should().save(captor.capture());
        PostEntity toSave = captor.getValue();
        assertThat(toSave.getTitle()).isEqualTo(cmd.title());
        assertThat(result.id()).isEqualTo(savedEntity.getId());
        assertThat(result.category().id()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("createPost should throw when category not found")
    void testCreatePostCategoryNotFound() {
        CreatePostCommand cmd = new CreatePostCommand("T", "C", "bad", List.of(tag1.getId()));
        given(categoryRepository.findById("bad")).willReturn(Optional.empty());
        assertThatThrownBy(() -> postService.createPost(cmd))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("updatePost should modify and return PostData")
    void testUpdatePostSuccess() {
        String postId = UlidCreator.getUlid().toString();
        PostEntity existing = new PostEntity(
                postId, "Old", "Old", category, Set.of(tag1), Instant.now(), Instant.now()
        );
        given(postRepository.findById(postId)).willReturn(Optional.of(existing));
        given(categoryRepository.findById(category.getId()))
                .willReturn(Optional.of(category));
        given(tagRepository.findAllById(List.of(tag2.getId())))
                .willReturn(List.of(tag2));

        UpdatePostCommand cmd = new UpdatePostCommand(
                "New", "NewContent", category.getId(), List.of(tag2.getId())
        );
        PostEntity updatedEntity = new PostEntity(
                postId, cmd.title(), cmd.content(), category,
                Set.of(tag2), existing.getCreatedAt(), Instant.now()
        );
        given(postRepository.save(any(PostEntity.class))).willReturn(updatedEntity);

        PostData result = postService.updatePost(postId, cmd);
        assertThat(result.title()).isEqualTo("New");
        assertThat(result.tags()).extracting(td -> td.name()).containsExactly("Spring");
    }

    @Nested
    class DeleteTests {
        @Test
        @DisplayName("deletePost should succeed when exists")
        void testDeletePostSuccess() {
            String id = "id1";
            given(postRepository.existsById(id)).willReturn(true);
            willDoNothing().given(postRepository).deleteById(id);
            assertThatCode(() -> postService.deletePost(id)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deletePost should throw when not exists")
        void testDeletePostNotFound() {
            given(postRepository.existsById("nope")).willReturn(false);
            assertThatThrownBy(() -> postService.deletePost("nope"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Test
    @DisplayName("getPostById should return Optional")
    void testGetPostById() {
        String id = "abc";
        PostEntity e = new PostEntity(
                id, "T", "C", category, Set.of(tag1), Instant.now(), Instant.now()
        );
        given(postRepository.findById(id)).willReturn(Optional.of(e));

        Optional<PostData> data = postService.getPostById(id);
        assertThat(data).isPresent();
        assertThat(data.get().id()).isEqualTo(id);
    }

    @Test
    @DisplayName("getAllPosts should list all")
    void testGetAllPosts() {
        PostEntity e1 = new PostEntity(
                UlidCreator.getUlid().toString(), "A", "B", category, Set.of(tag1), Instant.now(), Instant.now()
        );
        given(postRepository.findAll()).willReturn(List.of(e1));
        List<PostData> all = postService.getAllPosts();
        assertThat(all).hasSize(1).first().extracting(PostData::title).isEqualTo("A");
    }

    @Test
    @DisplayName("searchPosts should call repository and return matching")
    void testSearchPosts() {
        String term = "foo";
        PostEntity e = new PostEntity(
                UlidCreator.getUlid().toString(), "foo", "bar", category, Set.of(tag1), Instant.now(), Instant.now()
        );
        given(postRepository.searchByTerm("%foo%"))
                .willReturn(List.of(e));
        List<PostData> results = postService.searchPosts(term);
        assertThat(results).hasSize(1)
                .first().extracting(PostData::content).isEqualTo("bar");
    }

    @Test
    void whenGetPostByIdCalledTwice_thenRepositoryIsOnlyInvokedOnce() {
        // given
        String id = "cachedId";
        PostEntity e = new PostEntity(
                id, "T", "C", category, Set.of(tag1), Instant.now(), Instant.now()
        );
        given(postRepository.findById(id)).willReturn(Optional.of(e));

        // first call: hits the mock repository
        Optional<PostData> first = postService.getPostById(id);
        assertThat(first).isPresent();

        // second call: should be served from cache
        Optional<PostData> second = postService.getPostById(id);
        assertThat(second).isPresent();

        // verify repo called exactly once
        then(postRepository).should(times(1)).findById(id);
    }
}
package io.github.bluething.playground.java.bloggingplatformapi.persistence;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PostRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("CategoryRepository - save and find by ID")
    void testSaveAndFindCategory() {
        String id = UlidCreator.getUlid().toString();
        CategoryEntity cat = new CategoryEntity(id, "Tech");
        categoryRepository.save(cat);

        CategoryEntity fetched = categoryRepository.findById(id)
                .orElseThrow(() -> new AssertionError("Category not found"));
        assertThat(fetched.getName()).isEqualTo("Tech");
    }

    @Test
    @DisplayName("PostRepository - save with category and tags and retrieve with relationships")
    void testSavePostWithCategoryAndTags() {
        // prepare category
        String catId = UlidCreator.getUlid().toString();
        CategoryEntity cat = new CategoryEntity(catId, "Business");
        categoryRepository.save(cat);

        // prepare tags
        String tag1Id = UlidCreator.getUlid().toString();
        String tag2Id = UlidCreator.getUlid().toString();
        TagEntity tag1 = new TagEntity(tag1Id, "Java");
        TagEntity tag2 = new TagEntity(tag2Id, "JPA");
        tagRepository.saveAll(List.of(tag1, tag2));

        // flush to ensure relations
        entityManager.flush();
        entityManager.clear();

        // create post
        String postId = UlidCreator.getUlid().toString();
        Instant now = Instant.now();
        PostEntity post = new PostEntity(
                postId,
                "Repository Test",
                "Testing persistence layer",
                cat,
                Set.of(tag1, tag2),
                now,
                now
        );
        postRepository.save(post);
        entityManager.flush();
        entityManager.clear();

        // fetch post
        PostEntity fetched = postRepository.findById(postId)
                .orElseThrow(() -> new AssertionError("Post not found"));
        assertThat(fetched.getTitle()).isEqualTo("Repository Test");
        assertThat(fetched.getCategory().getId()).isEqualTo(catId);
        assertThat(fetched.getTags()).extracting(TagEntity::getName)
                .containsExactlyInAnyOrder("Java", "JPA");
    }

    @Nested
    class ConstraintTests {

        @Test
        @DisplayName("Category name unique constraint violation")
        void testCategoryNameUniqueConstraint() {
            String id1 = UlidCreator.getUlid().toString();
            String id2 = UlidCreator.getUlid().toString();
            CategoryEntity c1 = new CategoryEntity(id1, "UniqueCat");
            CategoryEntity c2 = new CategoryEntity(id2, "UniqueCat");
            categoryRepository.save(c1);
            // should throw on second save
            assertThatThrownBy(() -> {
                categoryRepository.saveAndFlush(c2);
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Test
    @DisplayName("Deleting post cascades post_tags entries")
    void testDeletePostCascadesTags() {
        // prepare category & tags
        String catId = UlidCreator.getUlid().toString();
        CategoryEntity cat = new CategoryEntity(catId, "Ops");
        categoryRepository.save(cat);

        String tagId = UlidCreator.getUlid().toString();
        TagEntity tag = new TagEntity(tagId, "Docker");
        tagRepository.save(tag);
        entityManager.flush();

        // create post
        String postId = UlidCreator.getUlid().toString();
        Instant now = Instant.now();
        PostEntity post = new PostEntity(postId, "Cascade Test", "Cascade content", cat, Set.of(tag), now, now);
        postRepository.save(post);
        entityManager.flush();

        // ensure join exists
        entityManager.clear();
        PostEntity fetched = postRepository.findById(postId).get();
        assertThat(fetched.getTags()).hasSize(1);

        // delete post
        postRepository.deleteById(postId);
        entityManager.flush();

        // verify join row removed
        List<PostEntity> remaining = entityManager.createQuery(
                        "SELECT p FROM PostEntity p WHERE p.id = :id", PostEntity.class)
                .setParameter("id", postId)
                .getResultList();
        assertThat(remaining).isEmpty();

        // also ensure no phantom relations in post_tags table
        Long bi = (Long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM post_tags WHERE post_id = ?")
                .setParameter(1, postId)
                .getSingleResult();
        assertThat(bi).isEqualTo(0L);
    }
}
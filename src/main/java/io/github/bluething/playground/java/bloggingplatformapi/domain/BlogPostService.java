package io.github.bluething.playground.java.bloggingplatformapi.domain;

import com.github.f4b6a3.ulid.UlidCreator;
import io.github.bluething.playground.java.bloggingplatformapi.exception.ResourceNotFoundException;
import io.github.bluething.playground.java.bloggingplatformapi.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class BlogPostService implements PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public PostData createPost(CreatePostCommand command) {
        CategoryEntity category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", command.categoryId()));
        List<TagEntity> tags = tagRepository.findAllById(command.tagIds());
        if (tags.size() != command.tagIds().size()) {
            throw new ResourceNotFoundException("Tag", String.join(",", String.join(",", command.tagIds())));
        }

        String id = UlidCreator.getUlid().toString();
        Instant now = Instant.now();
        Set<TagEntity> tagSet = new HashSet<>(tags);
        PostEntity entity = new PostEntity(
                id,
                command.title(),
                command.content(),
                category,
                tagSet,
                now,
                now
        );

        PostEntity saved = postRepository.save(entity);
        return toData(saved);
    }

    @Override
    @Transactional
    public PostData updatePost(String id, UpdatePostCommand command) {
        PostEntity existing = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));

        CategoryEntity category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", command.categoryId()));
        List<TagEntity> tags = tagRepository.findAllById(command.tagIds());
        if (tags.size() != command.tagIds().size()) {
            throw new ResourceNotFoundException("Tag", String.join(",", String.join(",", command.tagIds())));
        }

        Instant now = Instant.now();
        Set<TagEntity> tagSet = new HashSet<>(tags);
        PostEntity updated = new PostEntity(
                existing.getId(),
                command.title(),
                command.content(),
                category,
                tagSet,
                existing.getCreatedAt(),
                now
        );
        PostEntity saved = postRepository.save(updated);
        return toData(saved);
    }

    @Override
    @Transactional
    public void deletePost(String id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post", id);
        }
        postRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostData> getPostById(String id) {
        return postRepository.findById(id)
                .map(this::toData);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostData> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostData> searchPosts(String term) {
        String pattern = "%" + term.toLowerCase() + "%";
        return postRepository.searchByTerm(pattern).stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }
    /**
     * Map JPA entity to service-layer data transfer object.
     */
    PostData toData(PostEntity entity) {
        CategoryEntity cat = entity.getCategory();
        CategoryData categoryData = new CategoryData(
                cat.getId(),
                cat.getName()
        );
        List<TagData> tags = entity.getTags().stream()
                .map(tag -> new TagData(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
        return new PostData(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                categoryData,
                tags,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

package io.github.bluething.playground.java.bloggingplatformapi.persistence;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
public class PostEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;  // ULID

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PostEntity() {}

    public PostEntity(
            String id,
            String title,
            String content,
            CategoryEntity category,
            Set<TagEntity> tags,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}

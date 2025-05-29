package io.github.bluething.playground.java.bloggingplatformapi.persistence;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
public class TagEntity {
    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;  // ULID

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<PostEntity> posts;

    protected TagEntity() {}

    public TagEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

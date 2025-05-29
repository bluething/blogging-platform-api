package io.github.bluething.playground.java.bloggingplatformapi.persistence;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
public class CategoryEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    // bi-directional if needed
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<PostEntity> posts;

    protected CategoryEntity() {}

    public CategoryEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

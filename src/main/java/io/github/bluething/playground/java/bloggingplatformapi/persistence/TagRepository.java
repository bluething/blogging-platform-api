package io.github.bluething.playground.java.bloggingplatformapi.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, String> {
}

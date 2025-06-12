package io.github.bluething.playground.java.bloggingplatformapi.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, String> {
    /**
     * Search posts by a case-insensitive term matching title, content, or category name.
     * @param term a search pattern with SQL wildcards (e.g. "%tech%")
     * @return list of matching PostEntity
     */
    @Query("SELECT p FROM PostEntity p JOIN p.category c " +
            "WHERE LOWER(p.title) LIKE LOWER(:term) " +
            "OR LOWER(p.content) LIKE LOWER(:term) " +
            "OR LOWER(c.name) LIKE LOWER(:term)")
    List<PostEntity> searchByTerm(@Param("term") String term);

}

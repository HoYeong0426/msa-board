package msa.board.comment.repository;

import msa.board.comment.entity.CommentInfinite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentInfiniteRepository extends JpaRepository<CommentInfinite, Long> {

    @Query("select c from CommentInfinite c where c.commentPath.path = :path")
    Optional<CommentInfinite> findByPath(@Param("path") String path);

    @Query(
            value = "select path from comment_infinite " +
                    "where article_id = :articleId and path > :pathPrefix and path like :pathPrefix% " +
                    "order by path desc limit 1",
            nativeQuery = true
    )
    Optional<String> findDescendantsTopPath(
            @Param("articleId") Long articleId,
            @Param("pathPrefix") String pathPrefix
    );

    @Query(
            value = "select c.comment_id, c.content, c.path, c.article_id, " +
                    "c.writer_id, c.deleted, c.created_at " +
                    "from (" +
                    "   select comment_id from comment_infinite where article_id = :articleId " +
                    "   order by path asc " +
                    "   limit :limit offset :offset " +
                    ") t left join comment_infinite c on t.comment_id = c.comment_id",
            nativeQuery = true
    )
    List<CommentInfinite> findAll(
            @Param("articleId") Long articleId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    @Query(
            value = "select count(*) from(" +
                    "   select comment_id from comment_infinite where article_id = :articleId limit :limit " +
                    ") t",
            nativeQuery = true
    )
    Long count(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    @Query(
            value = "select c.comment_id, c.content, c.path, c.article_id, " +
                    "c.writer_id, c.deleted, c.created_at " +
                    "from comment_infinite c " +
                    "where article_id = :articleId " +
                    "order by path asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<CommentInfinite> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    @Query(
            value = "select c.comment_id, c.content, c.path, c.article_id, " +
                    "c.writer_id, c.deleted, c.created_at " +
                    "from comment_infinite c " +
                    "where article_id = :articleId and path > :lastPath " +
                    "order by path asc " +
                    "limit :limit",
            nativeQuery = true
    )
    List<CommentInfinite> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("lastPath") String lastPath,
            @Param("limit") Long limit
    );

}

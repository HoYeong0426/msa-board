package msa.board.comment.service;

import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import msa.board.comment.entity.CommentInfinite;
import msa.board.comment.entity.CommentPath;
import msa.board.comment.repository.CommentInfiniteRepository;
import msa.board.comment.service.request.CommentInfiniteCreateRequest;
import msa.board.comment.service.response.CommentPageResponse;
import msa.board.comment.service.response.CommentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentInfiniteService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentInfiniteRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentInfiniteCreateRequest request) {
        CommentInfinite parent = findParent(request);
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        // parentPah, descendantsTopPath -> childrenTopPath
        CommentInfinite comment = commentRepository.save(
                CommentInfinite.create(
                        snowflake.nextId(),
                        request.getContent(),
                        request.getArticleId(),
                        request.getWriterId(),
                        parentCommentPath.createChildCommentPath(
                                commentRepository.findDescendantsTopPath(request.getArticleId(), parentCommentPath.getPath())
                                        .orElse(null)
                        )
                )
        );

        return CommentResponse.from(comment);
    }

    private CommentInfinite findParent(CommentInfiniteCreateRequest request) {
        String parentPath = request.getParentPath();
        if (parentPath == null) {
            return null;
        }
        return commentRepository.findByPath(parentPath)
                .filter(not(CommentInfinite::getDeleted))
                .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(CommentInfinite::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(CommentInfinite comment) {
        return commentRepository.findDescendantsTopPath(
                comment.getArticleId(),
                comment.getCommentPath().getPath()
        ).isPresent();
    }

    private void delete(CommentInfinite comment) {
        // 1. 현재 댓글을 삭제
        commentRepository.delete(comment);

        // 2. 만약 루트 댓글이 아니라면 (즉, 대댓글이라면)
        if (!comment.isRoot()) {
            // 3. 부모 댓글을 찾아서
            commentRepository.findByPath(comment.getCommentPath().getParentPath())
                    // 4. 그 부모 댓글이 '삭제된 상태'라면 (getDeleted == true)
                    .filter(CommentInfinite::getDeleted)
                    // 5. 그리고 부모 댓글이 더 이상 다른 자식 댓글을 가지고 있지 않다면
                    .filter(not(this::hasChildren))
                    // 6. 부모 댓글도 같이 삭제
                    .ifPresent(this::delete);
        }
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponse> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize) {
        List<CommentInfinite> comments = lastPath == null ?
                commentRepository.findAllInfiniteScroll(articleId, pageSize) :
                commentRepository.findAllInfiniteScroll(articleId, lastPath, pageSize);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

}

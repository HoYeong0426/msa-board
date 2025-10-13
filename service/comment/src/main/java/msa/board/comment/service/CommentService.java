package msa.board.comment.service;

import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import msa.board.comment.entity.Comment;
import msa.board.comment.repository.CommentRepository;
import msa.board.comment.service.request.CommentCreateRequest;
import msa.board.comment.service.response.CommentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);

    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();

        if (parentCommentId == null) return null;

        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot)
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
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        // limit 2 -> Root 일 경우도 포함
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    private void delete(Comment comment) {
        // 1. 현재 댓글을 삭제
        commentRepository.delete(comment);

        // 2. 만약 루트 댓글이 아니라면 (즉, 대댓글이라면)
        if (!comment.isRoot()) {
            // 3. 부모 댓글을 찾아서
            commentRepository.findById(comment.getParentCommentId())
                    // 4. 그 부모 댓글이 '삭제된 상태'라면 (getDeleted == true)
                    .filter(Comment::getDeleted)
                    // 5. 그리고 부모 댓글이 더 이상 다른 자식 댓글을 가지고 있지 않다면
                    .filter(not(this::hasChildren))
                    // 6. 부모 댓글도 같이 삭제
                    .ifPresent(this::delete);
        }
    }
}

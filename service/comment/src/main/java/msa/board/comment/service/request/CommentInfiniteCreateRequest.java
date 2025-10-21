package msa.board.comment.service.request;

import lombok.Getter;

@Getter
public class CommentInfiniteCreateRequest {

    private Long articleId;
    private String content;
    private String parentPath;
    private Long writerId;
}

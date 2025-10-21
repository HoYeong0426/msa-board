package msa.board.comment.controller;

import lombok.RequiredArgsConstructor;
import msa.board.comment.service.CommentInfiniteService;
import msa.board.comment.service.request.CommentInfiniteCreateRequest;
import msa.board.comment.service.response.CommentPageResponse;
import msa.board.comment.service.response.CommentResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("msa/comments/infinite")
public class CommentInfiniteController {
    private final CommentInfiniteService commentService;

    @GetMapping("{commentId}")
    public CommentResponse read(
            @PathVariable("commentId") Long commentId
    ) {
        return commentService.read(commentId);
    }

    @PostMapping
    public CommentResponse create(@RequestBody CommentInfiniteCreateRequest request) {
        return commentService.create(request);
    }

    @DeleteMapping("{commentId}")
    public void delete(
            @PathVariable("commentId") Long commentId
    ) {
        commentService.delete(commentId);
    }

    @GetMapping
    public CommentPageResponse readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return commentService.readAll(articleId, page, pageSize);
    }

    @GetMapping("infinite-scroll")
    public List<CommentResponse> readAllInfiniteScroll(
            @RequestParam("articleId") Long articleId,
            @RequestParam(value = "lastPath", required = false) String lastPath,
            @RequestParam("pageSize") Long pageSize
    ) {
        return commentService.readAllInfiniteScroll(articleId, lastPath, pageSize);
    }







}

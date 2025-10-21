package msa.board.article.controller;

import lombok.RequiredArgsConstructor;
import msa.board.article.service.ArticleService;
import msa.board.article.service.request.ArticleCreateRequest;
import msa.board.article.service.request.ArticleUpdateRequest;
import msa.board.article.service.response.ArticlePageResponse;
import msa.board.article.service.response.ArticleResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("msa/articles")
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping("{articleId}")
    public ArticleResponse read(@PathVariable Long articleId) {
        return articleService.read(articleId);
    }

    @GetMapping
    public ArticlePageResponse readAll(
            @RequestParam("boardId") Long boardId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleService.readAll(boardId, page, pageSize);
    }

    @GetMapping("infinite-scroll")
    public List<ArticleResponse> readAllInfiniteScroll(
        @RequestParam("boardId") Long boardId,
        @RequestParam("pageSize") Long pageSize,
        @RequestParam(value = "lastArticleId", required = false) Long lastArticleId
    ) {
        return articleService.readAllInfiniteScroll(boardId, pageSize, lastArticleId);
    }

    @PostMapping
    public ArticleResponse create(@RequestBody ArticleCreateRequest request) {
        return articleService.create(request);
    }

    @PutMapping("{articleId}")
    public ArticleResponse update(@PathVariable Long articleId, @RequestBody ArticleUpdateRequest request) {
        return articleService.update(articleId, request);
    }

    @DeleteMapping("{articleId}")
    public void delete(@PathVariable Long articleId) {
        articleService.delete(articleId);
    }


}



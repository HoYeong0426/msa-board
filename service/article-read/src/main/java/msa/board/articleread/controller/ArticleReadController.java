package msa.board.articleread.controller;

import lombok.RequiredArgsConstructor;
import msa.board.articleread.service.ArticleReadService;
import msa.board.articleread.service.response.ArticleReadPageResponse;
import msa.board.articleread.service.response.ArticleReadResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("msa/articles")
public class ArticleReadController {
    private final ArticleReadService articleReadService;

    @GetMapping("{articleId}")
    public ArticleReadResponse read(@PathVariable("articleId") Long articleId) {
        return articleReadService.read(articleId);
    }

    @GetMapping
    public ArticleReadPageResponse readAll(
            @RequestParam("boardId") Long boardId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleReadService.readAll(boardId, page, pageSize);
    }

    @GetMapping("infinite-scroll")
    public List<ArticleReadResponse> readAllInfiniteScroll(
            @RequestParam("boardId") Long boardId,
            @RequestParam(value = "lastArticleId", required = false) Long lastArticleId,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleReadService.readAllInfiniteScroll(boardId, lastArticleId, pageSize);
    }

}

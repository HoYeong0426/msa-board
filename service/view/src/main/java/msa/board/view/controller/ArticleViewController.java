package msa.board.view.controller;

import lombok.RequiredArgsConstructor;
import msa.board.view.service.ArticleViewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("msa/article-views")
public class ArticleViewController {
    private final ArticleViewService articleViewService;

    @PostMapping("articles/{articleId}/users/{userId}")
    public Long increase(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        return articleViewService.increase(articleId, userId);
    }

    @GetMapping("articles/{articleId}/count")
    public Long count(@PathVariable("articleId") Long articleId) {
        return articleViewService.count(articleId);
    }
}

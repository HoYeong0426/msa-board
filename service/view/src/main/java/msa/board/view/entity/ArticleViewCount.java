package msa.board.view.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import msa.board.view.repository.ArticleViewCountRepository;

@Table(name = "article_view_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleViewCount {
    @Id
    private Long articleId;
    private Long viewCount;

    public static ArticleViewCount init(Long articleId, Long viewCount) {
        ArticleViewCount articleViewCount = new ArticleViewCount();
        articleViewCount.articleId = articleId;
        articleViewCount.viewCount = viewCount;
        return articleViewCount;
    }
}

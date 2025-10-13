package msa.board.article.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import msa.board.article.service.request.ArticleCreateRequest;
import msa.board.article.service.request.ArticleUpdateRequest;
import msa.board.article.service.response.ArticlePageResponse;
import msa.board.article.service.response.ArticleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class ArticleApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create((new ArticleCreateRequest(
                "title", "content", 1L, 1L
        )));

        System.out.println("response = " + response);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/atc/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void readTest() {
        ArticleResponse response = read(231396458644623360L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/atc/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(231396458644623360L, new ArticleUpdateRequest("title2", "content2"));
        ArticleResponse response = read(231396458644623360L);
        System.out.println("response = " + response);
    }

    void update(Long articleId, ArticleUpdateRequest articleUpdateRequest) {
        restClient.put()
                .uri("atc/articles/{articleId}", articleId)
                .body(articleUpdateRequest)
                .retrieve();
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/atc/articles/{articleId}", 231396458644623360L)
                .retrieve();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/atc/articles?boardId=1&pageSize=30&page=1")
                .retrieve()
                .body(ArticlePageResponse.class);

        System.out.println("response.getArticleCount() = " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()) {
            System.out.println("articleId = " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest() {
        List<ArticleResponse> articles1 = restClient.get()
                .uri("/atc/articles/infinite-scroll?boardId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        System.out.println("firstPage");
        for (ArticleResponse articleResponse : articles1) {
            System.out.println("articleResponse.getArticleId() = " + articleResponse.getArticleId());
        }

        Long lastArticleId = articles1.getLast().getArticleId();
        List<ArticleResponse> articles2 = restClient.get()
                .uri("/atc/articles/infinite-scroll?boardId=1&pageSize=5&lastArticleId=%s".formatted(lastArticleId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        System.out.println("second Page");
        for (ArticleResponse articleResponse : articles2) {
            System.out.println("articleResponse.getArticleId() = " + articleResponse.getArticleId());
        }
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }
}

package msa.board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.board.articleread.cache.OptimizedCacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewClient {
    private RestClient restClient;
    @Value("${endpoints.board-view-service.url}")
    private String viewServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(viewServiceUrl);
    }

//    @Cacheable(key = "#articleId", value = "articleViewCount")
    @OptimizedCacheable(type = "articleViewCount", ttlSeconds = 1)
    public long count(Long articleId) {
        try {
            return restClient.get()
                    .uri("msa/article-views/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch(Exception e) {
            log.error("[ViewClient.read] articleId={}", articleId, e);
            return 0;
        }
    }

}

package msa.board.comment.service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import msa.board.comment.entity.CommentPath;
import msa.board.comment.service.request.CommentCreateRequest;
import msa.board.comment.service.request.CommentInfiniteCreateRequest;
import msa.board.comment.service.response.CommentPageResponse;
import msa.board.comment.service.response.CommentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentInfiniteApiTest {

    RestClient restClient = RestClient.create("http://localhost:9001");


    @Test
    void create() {
        CommentResponse response1 = create(new CommentInfiniteCreateRequest(1L, "comment1", null, 1L));
        CommentResponse response2 = create(new CommentInfiniteCreateRequest(1L, "comment2", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentInfiniteCreateRequest(1L, "comment3", response2.getPath(), 1L));

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("\tresponse2.getPath() = " + response2.getPath());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getPath() = " + response3.getPath());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());

        /**
         * response1.getPath() = 00002
         * response1.getCommentId() = 237384337460772864
         * 	response2.getPath() = 0000200000
         * 	response2.getCommentId() = 237384338207358976
         * 		response3.getPath() = 000020000000000
         * 		response3.getCommentId() = 237384338282856448
         */

    }

    CommentResponse create(CommentInfiniteCreateRequest request) {
        return restClient.post()
                .uri("/msa/comments/infinite")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("msa/comments/infinite/{commentId}", 237384337460772864L)
                .retrieve()
                .body(CommentResponse.class);
        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/msa/comments/infinite/{commentId}", 237384337460772864L)
                .retrieve();
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/msa/comments/infinite?articleId=1&pageSize=10&page=50000")
                .retrieve()
                .body(CommentPageResponse.class);
        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId = " + comment.getCommentId());
        }

        /**
         * comment.getCommentId = 237387327975706628
         * comment.getCommentId = 237387328005066754
         * comment.getCommentId = 237387328005066760
         * comment.getCommentId = 237387328005066763
         * comment.getCommentId = 237387328005066768
         * comment.getCommentId = 237387328005066773
         * comment.getCommentId = 237387328005066777
         * comment.getCommentId = 237387328005066783
         * comment.getCommentId = 237387328009261056
         * comment.getCommentId = 237387328009261059
         */
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/msa/comments/infinite/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("firstPage");
        for (CommentResponse response : response1) {
            System.out.println("reseponse.getCommentId() = " + response.getCommentId());
        }

        String lastPath = response1.getLast().getPath();
        List<CommentResponse> response2 = restClient.get()
                .uri("/msa/comments/infinite/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("secondPage");
        for (CommentResponse response : response2) {
            System.out.println("reseponse.getCommentId() = " + response.getCommentId());
        }

        
    }

    @Getter
    @AllArgsConstructor
    public static class CommentInfiniteCreateRequest {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}

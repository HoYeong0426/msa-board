package msa.board.comment.service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import msa.board.comment.service.response.CommentPageResponse;
import msa.board.comment.service.response.CommentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(
                new CommentCreateRequest(1L, "comment1", null, 1L)
        );
        CommentResponse response2 = createComment(
                new CommentCreateRequest(1L, "comment2", response1.getCommentId(), 1L)
        );
        CommentResponse response3 = createComment(
                new CommentCreateRequest(1L, "comment3", response1.getCommentId(), 1L)
        );

        System.out.println("commentId=%s".formatted(response1.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response2.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response3.getCommentId()));

//        commentId=236143964883681280
//          commentId=236143965693181952
//          commentId=236143965798039552
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/msa/comments/{commentId}", 236143964883681280L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);

    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/msa/comments/{commentId}", 236143965798039552L)
                .retrieve()
                .body(CommentResponse.class);
    }


    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/msa/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/msa/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        /**
         * comment.getCommentId() = 236656451444203520
         * 	comment.getCommentId() = 236656451473563698
         * comment.getCommentId() = 236656451444203521
         * 	comment.getCommentId() = 236656451477758006
         * comment.getCommentId() = 236656451444203522
         * 	comment.getCommentId() = 236656451469369347
         * comment.getCommentId() = 236656451444203523
         * 	comment.getCommentId() = 236656451469369348
         * comment.getCommentId() = 236656451444203524
         * 	comment.getCommentId() = 236656451469369345
         */
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/msa/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("firstPage");
        for (CommentResponse comment : response1) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastParentCommentId = response1.getLast().getParentCommentId();
        Long lastCommentId = response1.getLast().getCommentId();

        List<CommentResponse> response2 = restClient.get()
                .uri("/msa/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
                        .formatted(lastParentCommentId, lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("secondPage");
        for (CommentResponse comment : response2) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }






    }

    @Getter
    @AllArgsConstructor
    public class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}

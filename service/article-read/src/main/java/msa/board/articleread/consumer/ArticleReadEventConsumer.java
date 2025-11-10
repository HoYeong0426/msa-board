package msa.board.articleread.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.board.articleread.service.ArticleReadService;
import msa.board.common.event.Event;
import msa.board.common.event.EventPayload;
import msa.board.common.event.EventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleReadEventConsumer {
    private final ArticleReadService articleReadService;

    @KafkaListener(
            topics = {
                    EventType.Topic.BOARD_ARTICLE,
                    EventType.Topic.BOARD_COMMENT,
                    EventType.Topic.BOARD_LIKE
            },
            groupId = "board-article-read-service"
    )
    public void listen(String message, Acknowledgment ack) {
        log.info("[ArticleReadEventConsumer.listen] message={}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            articleReadService.handleEvent(event);
        }
        ack.acknowledge();
    }
}

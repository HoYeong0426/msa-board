package msa.board.hotarticle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import msa.board.common.event.Event;
import msa.board.common.event.EventPayload;
import msa.board.common.event.EventType;
import msa.board.hotarticle.client.ArticleClient;
import msa.board.hotarticle.repository.HotArticleListRepository;
import msa.board.hotarticle.service.eventhandler.EventHandler;
import msa.board.hotarticle.service.response.HotArticleResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotArticleService {
    private final ArticleClient articleClient;
    private final List<EventHandler> eventHandlers;
    private final HotArticleScoreUpdater hotArticleScoreUpdator;
    private final HotArticleListRepository hotArticleListRepository;

    public void handleEvent(Event<EventPayload> event) {
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }

        if (isArticleCreateOrDeleted(event)) {
            eventHandler.handle(event);
        } else {
            hotArticleScoreUpdator.update(event, eventHandler);
        }

    }

    private boolean isArticleCreateOrDeleted(Event<EventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType() || EventType.ARTICLE_DELETED == event.getType();
    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }

    public List<HotArticleResponse> readAll(String dateStr) {
        return hotArticleListRepository.readAll(dateStr).stream()
                .map(articleClient::read)
                .filter(Objects::nonNull)
                .map(HotArticleResponse::from)
                .toList();
    }
}

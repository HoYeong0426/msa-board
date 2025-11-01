package msa.board.hotarticle.service;

import lombok.RequiredArgsConstructor;
import msa.board.common.event.Event;
import msa.board.common.event.EventPayload;
import msa.board.hotarticle.service.eventhandler.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotArticleScoreUpdater {
    

    public void update(Event<EventPayload> event, EventHandler<EventPayload> eventHandler) {

    }
}

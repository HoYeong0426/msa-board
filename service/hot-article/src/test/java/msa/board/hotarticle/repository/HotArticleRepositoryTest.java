package msa.board.hotarticle.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class HotArticleRepositoryTest {
    @Autowired
    HotArticleListRepository hotArticleRepository;

    @Test
    void addTest() throws InterruptedException {
        // given
        LocalDateTime time = LocalDateTime.of(2024, 7, 23, 0, 0);
        long limit = 3;

        // when
        hotArticleRepository.add(1L, time, 2L, limit, Duration.ofSeconds(3));
        hotArticleRepository.add(2L, time, 3L, limit, Duration.ofSeconds(3));
        hotArticleRepository.add(3L, time, 4L, limit, Duration.ofSeconds(3));
        hotArticleRepository.add(4L, time, 5L, limit, Duration.ofSeconds(3));
        hotArticleRepository.add(5L, time, 6L, limit, Duration.ofSeconds(3));
        hotArticleRepository.add(6L, time, 7L, limit, Duration.ofSeconds(3));

        // then
        List<Long> articleIds = hotArticleRepository.readAll("20240723");

        assertThat(articleIds).hasSize(Long.valueOf(limit).intValue());
        assertThat(articleIds.get(0)).isEqualTo(6);
        assertThat(articleIds.get(1)).isEqualTo(5);
        assertThat(articleIds.get(2)).isEqualTo(4);

        Thread.sleep(5000);

        assertThat(hotArticleRepository.readAll("20240723")).isEmpty();


    }

}
package msa.board.articleread.cache;

import lombok.RequiredArgsConstructor;
import msa.board.common.dataserializer.DataSerializer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final StringRedisTemplate redisTemplate;
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    private static final String DELIMITER = "::";

    public Object process(String type, long ttlSeconds, Object[] args, Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> originDataSupplier) throws Throwable {
        String key = generateKey(type, args);

        String cacheData = redisTemplate.opsForValue().get(key);
        // 캐시 데이터가 없으면 => 캐시 미스 → 원본 데이터(originDataSupplier)를 조회하고 캐시에 새로 저장
        if (cacheData == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        OptimizedCache optimizedCache = DataSerializer.deserialize(cacheData, OptimizedCache.class);
        // 역직렬화 실패 시(잘못된 데이터 등) → 캐시 재생성
        if (optimizedCache == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        // 캐시가 아직 유효(만료되지 않음)하면 → 캐시에서 데이터 바로 반환
        if (!optimizedCache.isExpired()) {
            return optimizedCache.parseData(returnType);
        }

        // 캐시가 만료된 경우 → 락 확인 (동시에 여러 요청이 들어올 때 한 번만 갱신하도록)
        if (!optimizedCacheLockProvider.lock(key)) {
            // 다른 프로세스가 이미 락을 잡고 갱신 중이면 기존 캐시 데이터 반환
            return optimizedCache.parseData(returnType);
        }

        try {
            // 락 획득 성공 → 원본 데이터 재조회 후 캐시 갱신
            return refresh(originDataSupplier, key, ttlSeconds);
        } finally {
            // 락 해제
            optimizedCacheLockProvider.unlock(key);
        }

    }

    private Object refresh(OptimizedCacheOriginDataSupplier<?> originDataSupplier, String key, long ttlSeconds) throws Throwable {
        Object result = originDataSupplier.get();

        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);
        OptimizedCache optimizedCache = OptimizedCache.of(result, optimizedCacheTTL.getLogicalTTL());

        redisTemplate.opsForValue()
                .set(
                        key,
                        DataSerializer.serialize(optimizedCache),
                        optimizedCacheTTL.getPhysicalTTL()
                );
        return result;
    }

    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER +
                Arrays.stream(args)
                        .map(String::valueOf)
                        .collect(Collectors.joining(DELIMITER));
    }
}

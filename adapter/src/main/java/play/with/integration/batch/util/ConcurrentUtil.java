package play.with.integration.batch.util;

import lombok.extern.slf4j.Slf4j;
import play.with.integration.batch.model.Person;
import play.with.integration.batch.model.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
public class ConcurrentUtil {

    public static List<Response> postAndGetResponseList(Stream<? extends Person> peopleStream, Function<String, Response> mapper) {
        Instant start = Instant.now();
        ForkJoinPool forkJoinPool = new ForkJoinPool(2);
        try {
            List<Response> responseList =
                    forkJoinPool.submit(
                            () -> peopleStream.map(Object::toString).map(mapper).collect(toList())
                    ).get();
            Duration duration = Duration.between(start, Instant.now());
            log.info("Thread - {}, duration- {}:{}, responses - {}", Thread.currentThread(), duration.toMinutes(), duration.getSeconds(), responseList.size());
            return responseList;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while processing people stream", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Error occurred while processing people stream", cause);
        } finally {
            forkJoinPool.shutdown();
        }
    }
}
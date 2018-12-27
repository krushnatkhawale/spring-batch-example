package play.with.integration.batch.util;

import play.with.integration.batch.model.Person;
import play.with.integration.batch.model.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ConcurrentUtil {

    public static List<Response> postAndGetResponseList(Stream<? extends Person> peopleStream, Function<String, Response> mapper) {
        try {
            Instant start = Instant.now();
            ForkJoinPool forkJoinPool = new ForkJoinPool(2);
            List<Response> responseList =
                    forkJoinPool.submit(
                            () -> peopleStream.map(Object::toString).map(mapper).collect(toList())
                    ).get();
            forkJoinPool.shutdown();
            forkJoinPool.awaitTermination(3, TimeUnit.SECONDS);
            Duration duration = Duration.between(start, Instant.now());
            System.out.println(String.format("W:-- %s -- %s:%s -- %s", Thread.currentThread(), duration.toMinutes(), duration.getSeconds(), responseList.size()));
            return responseList;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
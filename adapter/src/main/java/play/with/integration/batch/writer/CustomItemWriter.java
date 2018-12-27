package play.with.integration.batch.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import play.with.integration.batch.model.Person;
import play.with.integration.batch.model.Response;
import play.with.integration.batch.util.ConcurrentUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CustomItemWriter implements ItemWriter<Person>, StepExecutionListener {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomItemWriter.class);
    @Autowired
    RestTemplate restTemplate;

    @Value("${batch.endpoint}")
    private String endpoint;

    @Override
    public void write(List<? extends Person> items) throws ExecutionException, InterruptedException {
        //items.stream().map(Object::toString).forEach(LOGGER::info);

        ConcurrentUtil.postAndGetResponseList(items.parallelStream(), this::httpAction);

        //responseList.stream().map(Objects::toString).forEach(LOGGER::info);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().putString("JobSummary", stepExecution.getSummary());
        return stepExecution.getExitStatus();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    private Response httpAction(String item) {
        Instant start = Instant.now();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(endpoint, String.class);
        Duration duration = Duration.between(start, Instant.now());
        String timeTaken = duration.getSeconds() + ":" + duration.toMillis();
        return new Response(item, timeTaken, responseEntity.getStatusCode().toString());
    }
}
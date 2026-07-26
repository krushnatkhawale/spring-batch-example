package play.with.integration.batch.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import play.with.integration.batch.model.Person;
import play.with.integration.batch.model.Response;
import play.with.integration.batch.util.ConcurrentUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

public class CustomItemWriter implements ItemWriter<Person>, StepExecutionListener {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomItemWriter.class);
    @Autowired
    RestTemplate restTemplate;

    @Value("${batch.endpoint}")
    private String endpoint;

    @Override
    public void write(Chunk<? extends Person> chunk) throws ExecutionException, InterruptedException {
        ConcurrentUtil.postAndGetResponseList(chunk.getItems().parallelStream(), this::httpAction);
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
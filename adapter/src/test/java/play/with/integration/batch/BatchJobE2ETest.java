package play.with.integration.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import play.with.integration.flow.config.JobRequestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {
        "spring.application.name=batch-e2e-test",
        "batch.jobName=e2eTestJob",
        "batch.stepName=e2eTestStep",
        "batch.chunkSize=2",
        "batch.endpoint=http://localhost:8080/test",
        "spring.batch.job.enabled=false"
})
class BatchJobE2ETest {

    @Autowired
    private Job batchJob;

    @Autowired
    private JobOperator jobOperator;

    @MockitoBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        reset(restTemplate);
    }

    @Test
    void shouldCompleteJobSuccessfullyWithMultipleRecords() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        File tempFile = createTempInputFile("Alice", "Bob", "Charlie");

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("input.file.name", tempFile.getAbsolutePath())
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(batchJob, params);

            assertEquals(BatchStatus.COMPLETED, execution.getStatus());
            assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());

            var stepExecutions = execution.getStepExecutions().iterator().next();
            assertEquals(3, stepExecutions.getReadCount());
            assertEquals(3, stepExecutions.getWriteCount());
            assertEquals(0, stepExecutions.getSkipCount());
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    @Test
    void shouldCompleteJobWithSingleRecord() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        File tempFile = createTempInputFile("OnlyOne");

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("input.file.name", tempFile.getAbsolutePath())
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(batchJob, params);

            assertEquals(BatchStatus.COMPLETED, execution.getStatus());

            var stepExecutions = execution.getStepExecutions().iterator().next();
            assertEquals(1, stepExecutions.getReadCount());
            assertEquals(1, stepExecutions.getWriteCount());
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    @Test
    void shouldPreserveInputFileParameterWhenLaunchedThroughJobOperator() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        File tempFile = createTempInputFile("Alice");
        Path reportFile = reportFileFor(tempFile);

        try {
            JobRequestHelper helper = new JobRequestHelper();
            helper.setJob(batchJob);
            helper.setFileParameterName("input.file.name");
            JobLaunchRequest request = helper.toRequest(tempFile);

            JobExecution execution = jobOperator.start(request.getJob(), request.getJobParameters());

            assertEquals(tempFile.getAbsolutePath(),
                    execution.getJobParameters().getString("input.file.name"));
            assertNotNull(execution.getJobParameters().getLong("run.id"));
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());
            assertTrue(Files.exists(reportFile), "afterJob should write the report before the job returns");
        } finally {
            Files.deleteIfExists(tempFile.toPath());
            Files.deleteIfExists(reportFile);
        }
    }

    @Test
    void shouldFailWhenInputFileDoesNotExist() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("input.file.name", "/tmp/nonexistent_file_12345.txt")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobOperator.start(batchJob, params);

        assertEquals(BatchStatus.FAILED, execution.getStatus());
    }

    private File createTempInputFile(String... lines) throws IOException {
        File tempFile = File.createTempFile("batch-test-input-", ".txt");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), java.util.List.of(lines));
        return tempFile;
    }

    private Path reportFileFor(File inputFile) {
        String filename = inputFile.getName();
        return Path.of("Report-" + filename.substring(0, filename.lastIndexOf('.')) + ".json");
    }
}

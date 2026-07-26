package play.with.integration.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        "batch.endpoint=http://localhost:8080/test"
})
class BatchJobE2ETest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job batchJob;

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

            JobExecution execution = jobLauncher.run(batchJob, params);

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

            JobExecution execution = jobLauncher.run(batchJob, params);

            assertEquals(BatchStatus.COMPLETED, execution.getStatus());

            var stepExecutions = execution.getStepExecutions().iterator().next();
            assertEquals(1, stepExecutions.getReadCount());
            assertEquals(1, stepExecutions.getWriteCount());
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    @Test
    void shouldFailWhenInputFileDoesNotExist() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("input.file.name", "/tmp/nonexistent_file_12345.txt")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(batchJob, params);

        assertEquals(BatchStatus.FAILED, execution.getStatus());
    }

    private File createTempInputFile(String... lines) throws IOException {
        File tempFile = File.createTempFile("batch-test-input-", ".txt");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), java.util.List.of(lines));
        return tempFile;
    }
}

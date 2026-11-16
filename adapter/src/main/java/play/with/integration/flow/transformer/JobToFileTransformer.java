package play.with.integration.flow.transformer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
public class JobToFileTransformer {

    private static final String FILENAME_JOB_PARAMETER = "input.file.name";

    public File transformer(JobExecution jobExecution) {
        String inputFileName = jobExecution.getJobParameters().getString(FILENAME_JOB_PARAMETER);
        log.info("Extracting file [{}] from jobExecution", inputFileName);
        return Paths.get(inputFileName).toFile();
    }
}
package play.with.integration.flow.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;

import java.io.File;
import java.nio.file.Paths;

public class JobToFileTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobToFileTransformer.class);
    private static final String FILENAME_JOB_PARAMETER = "input.file.name";

    public File transformer(JobExecution jobExecution) {
        String inputFileName = jobExecution.getJobParameters().getString(FILENAME_JOB_PARAMETER);
        LOGGER.info("Extracting file [{}] from jobExecution", inputFileName);
        return Paths.get(inputFileName).toFile();
    }
}
package play.with.integration.flow.transformer;

import org.springframework.batch.core.JobExecution;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JobToFileTransformer {

    private static final String FILENAME_JOB_PARAMETER = "input.file.name";

    public File transformer(JobExecution jobExecution){
        Path path = Paths.get(jobExecution.getJobParameters().getString(FILENAME_JOB_PARAMETER));
        return path.toFile();
    }
}
package play.with.integration.flow.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;

public class JobStatusBasedRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStatusBasedRouter.class);

    public String transform(JobExecution jobExecution) {
        LOGGER.info("Job execution status: {}", jobExecution.getStatus());

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            return "completedJobChannel";
        }
        return "unCompletedJobChannel";
    }
}
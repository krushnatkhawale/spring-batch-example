package play.with.integration.flow.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.messaging.MessageChannel;

public class JobStatusBasedRouter {

    private Logger LOGGER = LoggerFactory.getLogger(JobStatusBasedRouter.class);

    private MessageChannel completedJobChannel;
    private MessageChannel unCompletedJobChannel;

    public JobStatusBasedRouter(MessageChannel completedJobChannel, MessageChannel unCompletedJobChannel) {
        this.completedJobChannel = completedJobChannel;
        this.unCompletedJobChannel = unCompletedJobChannel;
    }

    public String transform(JobExecution jobExecution) {
        LOGGER.info("Job execution status: {}", jobExecution.getStatus());

        if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
            return completedJobChannel.toString();
        }
        return unCompletedJobChannel.toString();
    }
}
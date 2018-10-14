package play.with.integration.flow.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.messaging.MessageChannel;

import java.util.List;

import static java.util.Collections.singletonList;

public class JobExecutionRouter {

    private Logger LOGGER = LoggerFactory.getLogger(JobExecutionRouter.class);

    private MessageChannel completedJobMessageChannel;
    private MessageChannel failedJobMessageChannel;

    public JobExecutionRouter(MessageChannel completedJobMessageChannel, MessageChannel failedJobMessageChannel) {
        this.completedJobMessageChannel = completedJobMessageChannel;
        this.failedJobMessageChannel = failedJobMessageChannel;
    }

    public List<String> transform(JobExecution jobExecution) {
        LOGGER.info("Job execution status: {}", jobExecution.getStatus());

        if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
            return singletonList(completedJobMessageChannel.toString());
        }
        return singletonList(failedJobMessageChannel.toString());
    }
}
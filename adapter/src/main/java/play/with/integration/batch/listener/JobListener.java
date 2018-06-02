package play.with.integration.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class JobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String summary = jobExecution.getExecutionContext().getString("JobSummary");
        System.out.println("Job summary: " + summary);
    }
}

package play.with.integration.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import play.with.integration.batch.model.Count;
import play.with.integration.batch.model.Report;
import play.with.integration.batch.model.StepReport;
import play.with.integration.batch.util.JsonUtils;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JobListener implements JobExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Report report = getJobReport(jobExecution);
        writeReportToFile(report);
        jobExecution.getExecutionContext().put("jobReport", report);
    }

    private void writeReportToFile(Report report) {
        try {
            String filename = report.getFilename();
            FileWriter fileWriter = new FileWriter(String.format("Report-%s.json", filename.substring(0, filename.lastIndexOf("."))));
            fileWriter.write(JsonUtils.toString(report));
            fileWriter.close();
        } catch (Exception e) {
            LOGGER.error("Error while writing job report: {}", e.getMessage());
        }
    }


    private Report getJobReport(JobExecution jobExecution) {
        JobParameters jobParameters = jobExecution.getJobParameters();
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        String inputFilePath = jobParameters.getString("input.file.name");
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        long readCount = stepExecutions.stream().mapToLong(StepExecution::getReadCount).sum();
        long readSkipCount = stepExecutions.stream().mapToLong(StepExecution::getReadSkipCount).sum();
        long processSkipCount = stepExecutions.stream().mapToLong(StepExecution::getProcessSkipCount).sum();
        long writeCount = stepExecutions.stream().mapToLong(StepExecution::getWriteCount).sum();
        long writeSkipCount = stepExecutions.stream().mapToLong(StepExecution::getWriteSkipCount).sum();

        Report report = new Report();
        report.setFilename(getFileName(inputFilePath));
        report.setStartTime(startTime);
        report.setEndTime(endTime);
        report.setJobStats(new Count(readCount, readSkipCount, processSkipCount, writeCount, writeSkipCount));
        List<StepReport> stepReports = stepExecutions.stream().map(StepReport::obtainStats).collect(Collectors.toList());
        report.setSteps(stepReports);
        return report;
    }

    private String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("\\") + 1);
    }
}

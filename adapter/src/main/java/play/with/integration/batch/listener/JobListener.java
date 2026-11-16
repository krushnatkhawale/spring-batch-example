package play.with.integration.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import play.with.integration.batch.model.Count;
import play.with.integration.batch.model.Report;
import play.with.integration.batch.model.StepReport;
import play.with.integration.batch.util.JsonUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("beforeJob");
        JobParameters jobParameters = jobExecution.getJobParameters();
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        String inputFilePath = jobParameters.getString("input.file.name");
        log.info("beforeJob complete, input file path: {}", inputFilePath);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("afterJob");
        Report report = getJobReport(jobExecution);
        writeReportToFile(report);
        jobExecution.getExecutionContext().put("jobReport", report);
        log.info("afterJob complete, report: {}", JsonUtils.toString(report));
    }

    private void writeReportToFile(Report report) {
        String filename = report.getFilename();
        int extensionIndex = filename.lastIndexOf('.');
        String reportBaseName = extensionIndex > 0 ? filename.substring(0, extensionIndex) : filename;
        Path reportPath = Path.of(String.format("report-%s.json", reportBaseName));
        try {
            Files.writeString(reportPath, JsonUtils.toString(report));
        } catch (IOException e) {
            throw new UncheckedIOException("Error while writing job report to " + reportPath, e);
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
        return Path.of(filePath.replace('\\', '/')).getFileName().toString();
    }
}

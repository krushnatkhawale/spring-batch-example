package play.with.integration.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import play.with.integration.batch.model.Count;
import play.with.integration.batch.model.Report;
import play.with.integration.batch.model.StepReport;
import play.with.integration.batch.util.JsonUtils;

import java.io.FileWriter;
import java.util.Collection;
import java.util.Date;
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
        Date startTime = jobExecution.getStartTime();
        Date endTime = jobExecution.getEndTime();

        int readCount = stepExecutions.stream().mapToInt(StepExecution::getReadCount).sum();
        int readSkipCount = stepExecutions.stream().mapToInt(StepExecution::getReadSkipCount).sum();
        int processSkipCount = stepExecutions.stream().mapToInt(StepExecution::getProcessSkipCount).sum();
        int writeCount = stepExecutions.stream().mapToInt(StepExecution::getWriteCount).sum();
        int writeSkipCount = stepExecutions.stream().mapToInt(StepExecution::getWriteSkipCount).sum();

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

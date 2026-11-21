package play.with.integration.metadata;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BatchMetadataTools {

    private static final int MAX_FILE_DETAILS = 100;

    private static final String DAILY_SUMMARY_SQL = """
            SELECT COUNT(*) AS TOTAL_JOBS,
                   SUM(CASE WHEN e.STATUS = 'COMPLETED' THEN 1 ELSE 0 END) AS COMPLETED_JOBS,
                   SUM(CASE WHEN e.STATUS <> 'COMPLETED' THEN 1 ELSE 0 END) AS UNSUCCESSFUL_JOBS,
                   SUM(CASE WHEN e.STATUS = 'COMPLETED' THEN COALESCE(s.READ_COUNT, 0) ELSE 0 END) AS RECORDS_READ,
                   SUM(CASE WHEN e.STATUS = 'COMPLETED' THEN COALESCE(s.WRITE_COUNT, 0) ELSE 0 END) AS RECORDS_WRITTEN
            FROM BATCH_JOB_EXECUTION e
            JOIN BATCH_JOB_EXECUTION_PARAMS p
              ON p.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID
             AND p.PARAMETER_NAME = 'input.file.name'
            LEFT JOIN (
                SELECT JOB_EXECUTION_ID, SUM(READ_COUNT) AS READ_COUNT, SUM(WRITE_COUNT) AS WRITE_COUNT
                FROM BATCH_STEP_EXECUTION
                GROUP BY JOB_EXECUTION_ID
            ) s ON s.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID
            WHERE e.END_TIME >= ? AND e.END_TIME < ?
            """;

    private static final String FILE_DETAILS_SQL = """
            SELECT p.PARAMETER_VALUE AS INPUT_FILE,
                   e.STATUS,
                   e.START_TIME,
                   e.END_TIME,
                   COALESCE(s.READ_COUNT, 0) AS RECORDS_READ,
                   COALESCE(s.WRITE_COUNT, 0) AS RECORDS_WRITTEN
            FROM BATCH_JOB_EXECUTION e
            JOIN BATCH_JOB_EXECUTION_PARAMS p
              ON p.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID
             AND p.PARAMETER_NAME = 'input.file.name'
            LEFT JOIN (
                SELECT JOB_EXECUTION_ID, SUM(READ_COUNT) AS READ_COUNT, SUM(WRITE_COUNT) AS WRITE_COUNT
                FROM BATCH_STEP_EXECUTION
                GROUP BY JOB_EXECUTION_ID
            ) s ON s.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID
            WHERE e.END_TIME >= ? AND e.END_TIME < ?
            ORDER BY e.END_TIME DESC
            LIMIT ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public BatchMetadataTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "Get counts of completed and unsuccessful batch files and record totals for today, with up to 100 most recent per-file details.")
    public DailyBatchSummary getTodaysBatchSummary() {
        return getBatchSummaryForDate(LocalDate.now());
    }

    @Tool(description = "Get counts of completed and unsuccessful batch files and record totals for a date, with up to 100 most recent per-file details.")
    public DailyBatchSummary getBatchSummaryForDate(
            @ToolParam(description = "Date to query in ISO format YYYY-MM-DD") String date) {
        return getBatchSummaryForDate(LocalDate.parse(date));
    }

    private DailyBatchSummary getBatchSummaryForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        Timestamp startTimestamp = Timestamp.valueOf(start);
        Timestamp endTimestamp = Timestamp.valueOf(end);

        SummaryCounts counts = jdbcTemplate.queryForObject(
                DAILY_SUMMARY_SQL,
                (resultSet, rowNumber) -> new SummaryCounts(
                        resultSet.getInt("TOTAL_JOBS"),
                        resultSet.getInt("COMPLETED_JOBS"),
                        resultSet.getInt("UNSUCCESSFUL_JOBS"),
                        resultSet.getLong("RECORDS_READ"),
                        resultSet.getLong("RECORDS_WRITTEN")),
                startTimestamp,
                endTimestamp);

        List<FileExecutionSummary> files = jdbcTemplate.query(
                FILE_DETAILS_SQL,
                (resultSet, rowNumber) -> new FileExecutionSummary(
                        resultSet.getString("INPUT_FILE"),
                        resultSet.getString("STATUS"),
                        resultSet.getObject("START_TIME", LocalDateTime.class),
                        resultSet.getObject("END_TIME", LocalDateTime.class),
                        resultSet.getLong("RECORDS_READ"),
                        resultSet.getLong("RECORDS_WRITTEN")),
                startTimestamp,
                endTimestamp,
                MAX_FILE_DETAILS);

        return new DailyBatchSummary(
                date,
                counts.completedJobs(),
                counts.unsuccessfulJobs(),
                counts.recordsRead(),
                counts.recordsWritten(),
                files,
                counts.totalJobs() > files.size());
    }

    private record SummaryCounts(
            int totalJobs,
            int completedJobs,
            int unsuccessfulJobs,
            long recordsRead,
            long recordsWritten) {
    }

    public record DailyBatchSummary(
            LocalDate date,
            int completedFiles,
            int unsuccessfulFiles,
            long recordsRead,
            long recordsWritten,
            List<FileExecutionSummary> files,
            boolean fileDetailsTruncated) {
    }

    public record FileExecutionSummary(
            String inputFile,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            long recordsRead,
            long recordsWritten) {
    }
}

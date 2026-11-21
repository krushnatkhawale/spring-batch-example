package play.with.integration.metadata;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchMetadataToolsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);

    private JdbcTemplate jdbcTemplate;
    private BatchMetadataTools tools;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:metadata-test;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION (
                    JOB_EXECUTION_ID BIGINT PRIMARY KEY,
                    STATUS VARCHAR(10),
                    START_TIME TIMESTAMP,
                    END_TIME TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS (
                    JOB_EXECUTION_ID BIGINT,
                    PARAMETER_NAME VARCHAR(100),
                    PARAMETER_VALUE VARCHAR(2500)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION (
                    JOB_EXECUTION_ID BIGINT,
                    READ_COUNT BIGINT,
                    WRITE_COUNT BIGINT
                )
                """);
        jdbcTemplate.update("DELETE FROM BATCH_STEP_EXECUTION");
        jdbcTemplate.update("DELETE FROM BATCH_JOB_EXECUTION_PARAMS");
        jdbcTemplate.update("DELETE FROM BATCH_JOB_EXECUTION");
        tools = new BatchMetadataTools(jdbcTemplate);
    }

    @Test
    void summarizesFinishedFileJobsAndTheirRecordCountsForDate() {
        insertExecution(1, "COMPLETED", "app-processed/a.txt", TODAY.atTime(8, 0), 12, 12);
        insertExecution(2, "COMPLETED", "app-processed/b.txt", TODAY.atTime(9, 0), 8, 7);
        insertExecution(3, "FAILED", "app-failed/c.txt", TODAY.atTime(10, 0), 3, 0);
        insertExecution(4, "COMPLETED", "app-processed/yesterday.txt", TODAY.minusDays(1).atTime(10, 0), 50, 50);

        BatchMetadataTools.DailyBatchSummary summary =
                tools.getBatchSummaryForDate(TODAY.toString());

        assertEquals(TODAY, summary.date());
        assertEquals(2, summary.completedFiles());
        assertEquals(1, summary.unsuccessfulFiles());
        assertEquals(20, summary.recordsRead());
        assertEquals(19, summary.recordsWritten());
        assertEquals(3, summary.files().size());
        assertFalse(summary.fileDetailsTruncated());
        assertEquals("app-failed/c.txt", summary.files().getFirst().inputFile());
    }

    @Test
    void rejectsDatesThatAreNotIsoFormatted() {
        assertThrows(DateTimeParseException.class, () -> tools.getBatchSummaryForDate("today"));
    }

    private void insertExecution(
            long id,
            String status,
            String filename,
            LocalDateTime endTime,
            long recordsRead,
            long recordsWritten) {
        LocalDateTime startTime = endTime.minusMinutes(1);
        jdbcTemplate.update(
                "INSERT INTO BATCH_JOB_EXECUTION (JOB_EXECUTION_ID, STATUS, START_TIME, END_TIME) VALUES (?, ?, ?, ?)",
                id,
                status,
                Timestamp.valueOf(startTime),
                Timestamp.valueOf(endTime));
        jdbcTemplate.update(
                "INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_VALUE) VALUES (?, ?, ?)",
                id,
                "input.file.name",
                filename);
        jdbcTemplate.update(
                "INSERT INTO BATCH_STEP_EXECUTION (JOB_EXECUTION_ID, READ_COUNT, WRITE_COUNT) VALUES (?, ?, ?)",
                id,
                recordsRead,
                recordsWritten);
    }
}

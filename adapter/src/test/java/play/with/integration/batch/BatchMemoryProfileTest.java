package play.with.integration.batch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("memory")
@TestPropertySource(properties = {
        "batch.endpoint=http://localhost:8080/test",
        "spring.batch.job.enabled=false"
})
class BatchMemoryProfileTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldInitializeBatchMetadataInMemory() {
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_JOB_INSTANCE", Integer.class));
    }
}

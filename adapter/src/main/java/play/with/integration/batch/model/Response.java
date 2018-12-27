package play.with.integration.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {
    private String requestedFor;
    private String timeTaken;
    private String responseCode;
}

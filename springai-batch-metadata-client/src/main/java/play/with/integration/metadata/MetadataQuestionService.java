package play.with.integration.metadata;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class MetadataQuestionService {

    private static final int MAX_QUESTION_LENGTH = 2_000;

    private final ChatClient chatClient;
    private final BatchMetadataTools batchMetadataTools;

    public MetadataQuestionService(
            ChatClient.Builder chatClientBuilder,
            BatchMetadataTools batchMetadataTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        Today is %s.
                        You answer questions about Spring Batch processing metadata.
                        Use the provided read-only metadata tools for all counts and file details.
                        Never invent database results or claim access to input file contents; metadata
                        stores counts and execution status, not the original record values. If a
                        question asks for information unavailable in the metadata, explain that.
                        Format answers as readable Markdown for a chat interface. Use a concise
                        summary followed by headings, bullet or numbered lists, and tables when they
                        make comparisons or multiple records easier to understand. Use **bold** and
                        *italic* emphasis sparingly, and fenced code blocks only for actual code or
                        technical values. Keep simple answers as short paragraphs. Do not output raw
                        HTML; use Markdown formatting only.
                        """.formatted(LocalDate.now()))
                .build();
        this.batchMetadataTools = batchMetadataTools;
    }

    public String answer(String question) {
        if (question == null || question.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question must not be blank");
        }
        if (question.length() > MAX_QUESTION_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "question must be at most " + MAX_QUESTION_LENGTH + " characters");
        }

        String answer = chatClient.prompt()
                .user(question)
                .tools(batchMetadataTools)
                .call()
                .content();
        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("The language model returned an empty answer");
        }
        return answer;
    }
}

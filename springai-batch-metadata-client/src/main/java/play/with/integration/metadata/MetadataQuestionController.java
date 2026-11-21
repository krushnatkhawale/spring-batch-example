package play.with.integration.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/metadata")
public class MetadataQuestionController {

    private final MetadataQuestionService questionService;

    public MetadataQuestionController(MetadataQuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/ask")
    public AnswerResponse ask(@RequestBody AskRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }
        return new AnswerResponse(questionService.answer(request.question()));
    }

    public record AskRequest(String question) {
    }

    public record AnswerResponse(String answer) {
    }
}

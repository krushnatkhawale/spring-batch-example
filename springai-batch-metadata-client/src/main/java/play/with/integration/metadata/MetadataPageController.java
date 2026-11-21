package play.with.integration.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class MetadataPageController {

    private final MetadataQuestionService questionService;
    private final MetadataAnswerFormatter answerFormatter;

    public MetadataPageController(
            MetadataQuestionService questionService,
            MetadataAnswerFormatter answerFormatter) {
        this.questionService = questionService;
        this.answerFormatter = answerFormatter;
    }

    @GetMapping("/")
    public String index() {
        return "metadata";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam(required = false) String question, Model model) {
        model.addAttribute("question", question == null ? "" : question);
        try {
            model.addAttribute("answerHtml", answerFormatter.toHtml(questionService.answer(question)));
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() != HttpStatus.BAD_REQUEST) {
                throw exception;
            }
            model.addAttribute("error", exception.getReason());
        }
        return "metadata";
    }
}

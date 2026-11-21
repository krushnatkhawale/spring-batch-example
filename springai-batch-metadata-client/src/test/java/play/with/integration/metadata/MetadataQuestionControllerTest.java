package play.with.integration.metadata;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetadataQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersTheThymeleafQuestionPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Spring Batch Metadata Assistant")));
    }

    @Test
    void rendersValidationErrorForBlankQuestions() throws Exception {
        mockMvc.perform(post("/ask")
                        .param("question", " "))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "question must not be blank")));
    }

    @Test
    void rejectsBlankQuestionsWithoutCallingTheLanguageModel() throws Exception {
        mockMvc.perform(post("/api/metadata/ask")
                        .contentType("application/json")
                        .content("""
                                {"question":" "}
                                """))
                .andExpect(status().isBadRequest());
    }
}

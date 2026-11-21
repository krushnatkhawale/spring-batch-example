package play.with.integration.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataAnswerFormatterTest {

    private final MetadataAnswerFormatter formatter = new MetadataAnswerFormatter();

    @Test
    void rendersMarkdownTablesListsAndEmphasis() {
        String html = formatter.toHtml("""
                **Summary**

                - First item
                - Second item

                | File | Status |
                | --- | --- |
                | input.csv | *Completed* |
                """);

        assertTrue(html.contains("<strong>Summary</strong>"));
        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("<em>Completed</em>"));
    }

    @Test
    void escapesRawHtmlFromTheModel() {
        String html = formatter.toHtml("<script>alert('unsafe')</script>");

        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(!html.contains("<script>"));
    }
}

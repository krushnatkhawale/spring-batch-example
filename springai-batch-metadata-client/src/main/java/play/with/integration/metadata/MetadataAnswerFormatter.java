package play.with.integration.metadata;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MetadataAnswerFormatter {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MetadataAnswerFormatter() {
        List<Extension> extensions = List.of(TablesExtension.create());
        this.parser = Parser.builder()
                .extensions(extensions)
                .build();
        this.renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .escapeHtml(true)
                .build();
    }

    public String toHtml(String markdown) {
        return renderer.render(parser.parse(markdown));
    }
}

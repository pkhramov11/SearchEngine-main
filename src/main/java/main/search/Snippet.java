package main.search;

import main.model.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Snippet {
    private final Page page;
    private final List<String> queryWords;
    private final List<OwnText> ownTexts = new ArrayList<>();

    public Snippet(Page page, List<String> queryWords) {
        this.page = page;
        this.queryWords = queryWords;
    }

    public String formSnippet() {
        Document doc = Jsoup.parse(page.getContent());
        StringBuilder builder = new StringBuilder();
        for (String field : Arrays.asList("title", "body")) {
            Element element = doc.getElementsByTag(field).first();
            if (field.equals("title")) {
                if (element != null) {
                    page.setTitle(element.text());
                }
                continue;
            }
            createOwnTexts(element);
            for (OwnText ownText : ownTexts) {
                ownText.defineQueryWordIndices(queryWords);
                if (ownText.containsQueryWords()) {
                    ownText.createFragments();
                    ownText.formCompositionOfFragments(builder);
                }
            }
        }
        return builder.toString();
    }

    private void createOwnTexts(Element element) {
        String ownText = element.ownText();
        if (!ownText.isEmpty()) {
            ownTexts.add(new OwnText(ownText));
        }
        for (Element child : element.children()) {
            createOwnTexts(child);
        }
    }
}

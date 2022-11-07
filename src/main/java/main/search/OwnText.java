package main.search;

import lombok.Data;
import main.lemmatizator.Lemmatizator;

import java.util.*;

@Data
public class OwnText {
    public static final int SEPARATORS_BEFORE_COUNT = 5;
    public static final int SEPARATORS_AFTER_COUNT = 5;
    public static final String BOLD_BEGIN = "<b>";
    public static final String BOLD_END = "</b>";
    public static final String SPACE = " ";
    public static final String ELLIPSIS = "...";

    private enum SeparatorType {
        SEPARATOR, ELEMENT_BEGIN, ELEMENT_END, SENTENCE_BEGIN, SENTENCE_END,
        PREVIOUS_END, BOLD_BEGIN, BOLD_END
    }

    private String text;
    private List<Integer> queryWordIndices = new ArrayList<>();

    public OwnText(String text) {
        this.text = text;
    }

    public boolean containsQueryWords() {
        return queryWordIndices.size() > 0;
    }

    private static final Set<Character> sentanceEnds =
            new HashSet<>(Arrays.asList('.', '!', '?'));
    private static final Set<Character> separators =
            new HashSet<>(Arrays.asList(' ', ',', ';', '-', '–', '—'));
    private static final Set<Character> allSeparators = new HashSet<>(
            Arrays.asList('.', '!', '?', ' ', ',', ';', '-', '–', '—'));

    public void defineQueryWordIndices(List<String> queryWords) {
        String[] contentWords = text.split(Lemmatizator.WORD_SEPARATORS);
        StringBuilder builder = new StringBuilder();
        int indexInText = 0;
        for (String contentWord : contentWords) {
            if (contentWord.isEmpty()) {
                continue;
            }
            List<String> contentForms = Lemmatizator.processOneWord(contentWord);
            for (String form : contentForms) {
                if (queryWords.contains(form)) {
                    int index = text.indexOf(contentWord, indexInText);
                    if (index < 0) {
                        continue;
                    }
                    builder.append(text.substring(indexInText, index));
                    queryWordIndices.add(builder.length());
                    builder.append(BOLD_BEGIN);
                    builder.append(contentWord);
                    builder.append(BOLD_END);
                    queryWordIndices.add(builder.length());
                    indexInText = index + contentWord.length();
                }
            }
        }
        builder.append(text.substring(indexInText));
        text = builder.toString();
    }

    private static class Fragment {
        private String text;
        private int startPosition;
        private int endPosition;
        private SeparatorType leftSeparator;
        private SeparatorType rightSeparator;
    }

    public void formCompositionOfFragments(StringBuilder builder) {
        for (Fragment fragment : fragments) {
            String prefix = "";
            switch (fragment.leftSeparator) {
                case ELEMENT_BEGIN, PREVIOUS_END -> prefix = " ";
                case SEPARATOR, SENTENCE_BEGIN -> prefix = ELLIPSIS + SPACE;
            }
            builder.append(prefix);
            builder.append(fragment.text);
        }
        Fragment fragment = fragments.get(fragments.size() - 1);
        String postfix = "";
        if (fragment.rightSeparator == SeparatorType.SEPARATOR) {
            postfix = SPACE + ELLIPSIS;
        }
        builder.append(postfix);
    }

    public void createFragments() {
        for (int queryWordIndex = 0; queryWordIndex < queryWordIndices.size(); queryWordIndex++) {
            Fragment fragment = new Fragment();
            findFragmentStart(queryWordIndex);
            fragment.startPosition = position;
            fragment.leftSeparator = leftSeparatorType;
            int processedQueryWords = findFragmentEnd(++queryWordIndex);
            fragment.endPosition = position;
            endOfPreviousFragment = position;
            fragment.rightSeparator = rightSeparatorType;
            queryWordIndex += (processedQueryWords - 1) * 2;
            fragment.text = text.substring(fragment.startPosition, fragment.endPosition);
            fragments.add(fragment);
        }
    }

    private int findFragmentEnd(int queryWordIndex) {
        int processedQueryWords = 1;
        position = queryWordIndices.get(queryWordIndex);
        for (int count = 0; count < SEPARATORS_BEFORE_COUNT; count++) {
            if (position == text.length()) {
                rightSeparatorType = SeparatorType.ELEMENT_END;
                return processedQueryWords;
            }
            missSeparatorsRight();
            if (position == text.length()) {
                count++;
                continue;
            }
            if (sentanceEnds.contains(getChar())) {
                position++;
                rightSeparatorType = SeparatorType.SENTENCE_END;
                return processedQueryWords;
            }
            if (text.startsWith(BOLD_BEGIN, position)) {
                processedQueryWords++;
                count = 0;
            }
            missOneWordRight();
        }
        rightSeparatorType = SeparatorType.SEPARATOR;
        return processedQueryWords;
    }

    private void findFragmentStart(int queryWordIndex) {
        position = queryWordIndices.get(queryWordIndex);
        for (int count = 0; count < SEPARATORS_BEFORE_COUNT; count++) {
            if (position == 0) {
                leftSeparatorType = SeparatorType.ELEMENT_BEGIN;
                return;
            }
            if (position == endOfPreviousFragment) {
                leftSeparatorType = SeparatorType.PREVIOUS_END;
                return;
            }
            missSeparatorsLeft();
            if (position == 0) {
                count++;
                continue;
            }
            if (sentanceEnds.contains(getChar())) {
                stepToWordBegin();
                leftSeparatorType = SeparatorType.SENTENCE_BEGIN;
                return;
            }
            missOneWordLeft();
        }
        leftSeparatorType = SeparatorType.SEPARATOR;
    }

    private char getChar() {
        currentChar = text.charAt(position);
        return currentChar;
    }

    private void stepToWordBegin() {
        while (position < text.length()) {
            if (!allSeparators.contains(getChar())) {
                return;
            }
            position++;
        }
    }

    private void missSeparatorsLeft() {
        while (--position >= 0) {
            if (!separators.contains(getChar())) {
                return;
            }
        }
        position = 0;
    }

    private void missSeparatorsRight() {
        if (sentanceEnds.contains(getChar())) {
            return;
        }
        while (++position < text.length()) {
            if (!separators.contains(getChar())) {
                return;
            }
        }
    }

    private void missOneWordRight() {
        while (!allSeparators.contains(getChar())) {
            if (++position == text.length()) {
                return;
            }
        }
    }

    private void missOneWordLeft() {
        while (!allSeparators.contains(getChar())) {
            if (--position < 0) {
                position = 0;
                return;
            }
        }
    }

    private List<Fragment> fragments = new ArrayList<>();
    private int endOfPreviousFragment = 0;
    private int position;
    private SeparatorType leftSeparatorType;
    private SeparatorType rightSeparatorType;
    private char currentChar;
}

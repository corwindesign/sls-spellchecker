package com.isahb.slsspellchecker.model;


import java.util.ArrayList;
import java.util.List;

import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.IncorrectExample;

/**
 * isahb
 */
public class SpellCheckResult {
    private String error;
    private List<Match> matches = new ArrayList<>();

    public String getError() {
        return error;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void addCorrectionSuggestion(Match match) {
        this.matches.add(match);
    }

    private SpellCheckResult(String errorMsg) {
        this.error = errorMsg;
    }

    public static SpellCheckResult withError(String errorMsg) {
        return new SpellCheckResult(errorMsg);
    }

    public SpellCheckResult() {
    }

    public static class Match {
        private int startPos;
        private int endPos;
        private String message;
        private String shortMessage;
        private String type;
        private List<Replacement> replacements = new ArrayList<>();
        private Rule rule;
        private int length;
        private int offset;

        // required by Jackson
        public Match() {
        }

        public Match(int startPos, int endPos, String message, String shortMessage, String type,
                Rule suggestionMeta, List<String> replacements) {
            this.length = endPos - startPos;
            this.startPos = startPos;
            this.offset = startPos;
            this.endPos = endPos;
            this.message = message;
            this.shortMessage = shortMessage;
            this.type = type;
            this.rule = suggestionMeta;
            for (String s : replacements) {
                if (s != null) {
                    this.replacements.add(new Replacement(s));
                }
            }
        }

        public int getStartPos() {
            return startPos;
        }

        public int getEndPos() {
            return endPos;
        }

        public String getMessage() {
            return message;
        }

        public List<Replacement> getReplacements() {
            return replacements;
        }

        public String getShortMessage() {
            return shortMessage;
        }

        public String getType() {
            return type;
        }

        public Rule getRule() {
            return rule;
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return offset;
        }
    }

    public static class Rule {
        private String categoryId;
        private String categoryName;
        private List<CorrectExample> correctExamples = new ArrayList<>();
        private List<IncorrectExample> incorrectExamples = new ArrayList<>();
        private String id;

        public Rule() {
        }

        public Rule(String categoryId, String categoryName, List<CorrectExample> correctExamples,
                List<IncorrectExample> incorrectExamples, String ruleId) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.correctExamples = correctExamples;
            this.incorrectExamples = incorrectExamples;
            this.id = ruleId;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public List<CorrectExample> getCorrectExamples() {
            return correctExamples;
        }

        public List<IncorrectExample> getIncorrectExamples() {
            return incorrectExamples;
        }

        public String getId() {
            return id;
        }
    }
    
    public static class Replacement {
        private String value;

        public Replacement() {

        }

        public Replacement(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

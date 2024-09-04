package com.isahb.slsspellchecker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Spanish;
import org.languagetool.rules.Category;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.isahb.slsspellchecker.model.ApiGatewayResponse;
import com.isahb.slsspellchecker.model.Response;
import com.isahb.slsspellchecker.model.SpellCheckResult;


/**
 * @author isahb
 */
public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final Logger LOG = LogManager.getLogger(Handler.class);
    private static final int CHARACTER_LIMIT = 5555;
    public static final Map<String, String> HEADERS = new HashMap<>();
    static {
        HEADERS.put("X-Powered-By", "Languagetool");
        HEADERS.put("Access-Control-Allow-Origin", "*");
    }

    private JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
    private ObjectMapper objectMapper = initializeObjectMapper();

    private ObjectMapper initializeObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("received: {}", input);
        try {
            String bodyJson = input.getOrDefault("body", "{}").toString();
            Map<String, String> requestBody = objectMapper.readerFor(Map.class).readValue(bodyJson);
            String text = requestBody.get("text");
            String language = requestBody.get("language");
            if (language == "es") {
                langTool = new JLanguageTool(new Spanish());
            }
            if (text == null || text.length() > CHARACTER_LIMIT) {
                String errorMsg = String.format("Max length of input should be %d", CHARACTER_LIMIT);
                return ApiGatewayResponse.builder()
                        .setObjectBody(SpellCheckResult.withError(errorMsg)).setStatusCode(400).setHeaders(HEADERS).build();
            }
            List<RuleMatch> matches = langTool.check(text);
            LOG.info("matches: {}", matches);
            SpellCheckResult spellCheckResult = new SpellCheckResult();
            matches.stream().forEach(match -> {
                int fromPos = match.getFromPos();
                int toPos = match.getToPos();
                String message = match.getMessage();
                String shortMessage = match.getShortMessage();
                String name = match.getType().name();
                List<String> suggestedReplacements = match.getSuggestedReplacements();
                Rule rule = match.getRule();
                Category category = rule.getCategory();
                SpellCheckResult.Rule suggestionMeta = new SpellCheckResult.Rule(category.getId().toString(), category.getName(), rule.getCorrectExamples(), rule.getIncorrectExamples(), rule.getId());
                SpellCheckResult.Match spellCheckMatch = new SpellCheckResult.Match(fromPos, toPos, message, shortMessage, name, suggestionMeta, suggestedReplacements);
                spellCheckResult.addCorrectionSuggestion(spellCheckMatch);
            });
            return ApiGatewayResponse.builder().setStatusCode(200).setObjectBody(spellCheckResult)
                    .setHeaders(HEADERS).build();
        } catch (IOException e) {
            LOG.error("Error", e);
            return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(new Response("IO Error", new HashMap<>()))
                    .setHeaders(HEADERS).build();
        }
    }
}

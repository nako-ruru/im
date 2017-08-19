package com.mycompany.im.compute.adapter.service;

import com.google.common.io.CharStreams;
import com.ijimu.capital.BlackKeyword;
import com.mycompany.im.compute.domain.KeywordHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/7/15.
 */
@Service
public class KeywordHandlerImpl implements KeywordHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordHandlerImpl.class.getName());

    private BlackKeyword blackKeyword = new BlackKeyword();

    public KeywordHandlerImpl() {
        try(Reader reader = new InputStreamReader(getClass().getResourceAsStream("keyword.txt"))) {
            String content = CharStreams.toString(reader);
            Pattern compile = Pattern.compile("\\(\\s*\\d+\\s*,\\s*['\"](.+?)['\"]\\)");
            Matcher matcher = compile.matcher(content);
            List<String> keywords = new LinkedList<>();
            while(matcher.find()) {
                keywords.add(matcher.group(1));
            }
            blackKeyword = new BlackKeyword(keywords);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public String handle(String input) {
        return blackKeyword.checkAndReplace(input);
    }

}

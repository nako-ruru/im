package com.mycompany.im.compute.adapter.service;

import com.google.common.io.CharStreams;
import com.ijimu.capital.BlackKeyword;
import com.mycompany.im.compute.KeyWorldHandler;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/7/15.
 */
public class KeyWorldHandlerImpl implements KeyWorldHandler {

    private static final Logger LOGGER = Logger.getLogger(KeyWorldHandlerImpl.class .getName());

    private BlackKeyword blackKeyword = new BlackKeyword();

    private JavaSparkContext ctx = new JavaSparkContext(
            "local",
            "JavaWordCount",
            System.getenv("SPARK_HOME"),
            JavaSparkContext.jarOfClass(getClass())
    );

    public KeyWorldHandlerImpl() {
        try(InputStream in = getClass().getResourceAsStream("/com/mycompany/im/compute/adapter/service/keyword.txt")) {
            String content = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
            Pattern compile = Pattern.compile("\\(\\s*\\d+\\s*,\\s*['\"](.+?)['\"]\\)");
            Matcher matcher = compile.matcher(content);
            List<String> keywords = new LinkedList<>();
            while(matcher.find()) {
                keywords.add(matcher.group(1));
            }
            blackKeyword = new BlackKeyword(keywords);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public String handle(String input) {
        JavaRDD<String> lines = ctx.parallelize(Arrays.asList(input));
        lines.cache();
        return lines.map(blackKeyword::checkAndReplace)
                .first();
    }

}

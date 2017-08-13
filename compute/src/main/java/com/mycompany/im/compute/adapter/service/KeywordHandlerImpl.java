package com.mycompany.im.compute.adapter.service;

import com.google.common.io.CharStreams;
import com.ijimu.capital.BlackKeyword;
import com.mycompany.im.compute.domain.KeywordHandler;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
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

    private JavaSparkContext ctx;
    private JavaRDD<BlackKeyword> rdd;

    public KeywordHandlerImpl() {
        SparkConf conf = new SparkConf();
        conf.setMaster("local");
        conf.setAppName("compute");
        conf.set("spark.serializer", "org.apache.spark.serializer.JavaSerializer");
        ctx = new JavaSparkContext(conf);

        try(Reader reader = new InputStreamReader(getClass().getResourceAsStream("keyword.txt"))) {
            String content = CharStreams.toString(reader);
            Pattern compile = Pattern.compile("\\(\\s*\\d+\\s*,\\s*['\"](.+?)['\"]\\)");
            Matcher matcher = compile.matcher(content);
            List<String> keywords = new LinkedList<>();
            while(matcher.find()) {
                keywords.add(matcher.group(1));
            }

            Broadcast<BlackKeyword> broadcast = ctx.broadcast(new BlackKeyword(keywords));
            rdd = ctx.parallelize(Arrays.asList(broadcast))
                    .map(b -> b.value());
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public String handle(String input) {
        String result = rdd
                .map(keyword -> keyword.checkAndReplace(input))
                .first();
        return result;
    }

}

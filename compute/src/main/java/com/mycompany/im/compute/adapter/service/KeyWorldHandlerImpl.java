package com.mycompany.im.compute.adapter.service;

import com.github.fge.lambdas.consumers.ThrowingBiConsumer;
import com.google.common.io.CharStreams;
import com.ijimu.capital.BlackKeyword;
import com.mycompany.im.compute.domain.KeyWorldHandler;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by Administrator on 2017/7/15.
 */
@Service
public class KeyWorldHandlerImpl implements KeyWorldHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyWorldHandlerImpl.class .getName());

    private JavaSparkContext ctx = new JavaSparkContext(
            "local",
            "JavaWordCount",
            System.getenv("SPARK_HOME"),
            JavaSparkContext.jarOfClass(getClass())
    );
    JavaRDD<Broadcast<BlackKeywordWrapper>> cache;

    public KeyWorldHandlerImpl() {
        try(InputStream in = getClass().getResourceAsStream("/com/mycompany/im/compute/adapter/service/keyword.txt")) {
            String content = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
            Pattern compile = Pattern.compile("\\(\\s*\\d+\\s*,\\s*['\"](.+?)['\"]\\)");
            Matcher matcher = compile.matcher(content);
            List<String> keywords = new LinkedList<>();
            while(matcher.find()) {
                keywords.add(matcher.group(1));
            }

            Broadcast<BlackKeywordWrapper> broadcast = ctx.broadcast(new BlackKeywordWrapper(new BlackKeyword(keywords)));
            cache = ctx.parallelize(Arrays.asList(broadcast))
                    .cache();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public String handle(String input) {
        String result = cache.map(keyword -> keyword.value().checkAndReplace(input))
                .first();
        return result;
    }
    
    private static class BlackKeywordWrapper implements Externalizable {

        private BlackKeyword blackKeyword = new BlackKeyword();
        private byte[] cached = null;

        public BlackKeywordWrapper() {
        }

        public BlackKeywordWrapper(BlackKeyword blackKeyword) {
            this.blackKeyword = blackKeyword;
        }

        public String checkAndReplace(String article) {
            return blackKeyword.checkAndReplace(article);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            if(cached == null) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream tout = new DataOutputStream(baos);
                int size = 0;
                Map<String, Object> firstCharHash = ObjectFieldHelpers.getValue(blackKeyword, "firstCharHash");
                if(firstCharHash == null) {
                    tout.writeInt(size);
                    return;
                }
                tout.writeInt(firstCharHash.size());
                firstCharHash.forEach((ThrowingBiConsumer<String, Object>)(k, v) -> {
                    tout.writeUTF(k);
                    Object p = v;
                    int i = 0;
                    while(p != null) {
                        i++;
                        p = ObjectFieldHelpers.getValue(p, "next");
                    }
                    tout.writeInt(i);
                    p = v;
                    while(p != null) {
                        tout.writeUTF(ObjectFieldHelpers.getValue(p, "text"));
                        p = ObjectFieldHelpers.getValue(p, "next");
                    }
                });

                tout.flush();
                cached = baos.toByteArray();
            }

            out.write(cached);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            int size = in.readInt();
            Map<String, Object> firstCharHash = new HashMap<>(4096);
            for(int i = 0; i < size; i++) {
                String k = in.readUTF();
                int nodeCount = in.readInt();
                Object p = null;
                for(int j = 0; j < nodeCount; j++) {
                    Object n = ObjectFieldHelpers.create("com.ijimu.capital.BlackKeyword$BlackWordHashNode");
                    String text = in.readUTF();
                    ObjectFieldHelpers.setValue(n, "text", text);
                    if(j == 0) {
                        firstCharHash.put(k, n);
                    } else {
                        ObjectFieldHelpers.setValue(p, "next", n);
                    }
                    p = n;
                }
            }

            ObjectFieldHelpers.setValue(blackKeyword, "firstCharHash", firstCharHash);
        }

        private static class ObjectFieldHelpers implements Serializable {

            private static Object create(String className) {
                try {
                    Class clazz = Class.forName(className);
                    Constructor constructor = Stream.of(clazz.getDeclaredConstructors())
                            .filter(c -> c.getParameterCount() == 0)
                            .findAny()
                            .get();
                    if(!constructor.isAccessible()) {
                        constructor.setAccessible(true);
                    }
                    return constructor.newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new Error(e);
                }
            }

            private static <T> T getValue(Object o, String filedName) {
                Class clazz = o.getClass();
                try {
                    Field textFiled = clazz.getDeclaredField(filedName);
                    if(!textFiled.isAccessible()) {
                        textFiled.setAccessible(true);
                    }
                    return (T) textFiled.get(o);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new Error(e);
                }
            }

            private static void setValue(Object o, String filedName, Object value) {
                Class clazz = o.getClass();
                try {
                    Field textFiled = clazz.getDeclaredField(filedName);
                    if(!textFiled.isAccessible()) {
                        textFiled.setAccessible(true);
                    }
                    textFiled.set(o, value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new Error(e);
                }
            }


        }

    }

}

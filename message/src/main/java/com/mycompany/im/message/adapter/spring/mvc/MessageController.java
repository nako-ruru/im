package com.mycompany.im.message.adapter.spring.mvc;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mycompany.im.message.application.MessageParameter;
import com.mycompany.im.message.application.MessageQuery;
import com.mycompany.im.message.application.MessageResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/5/13.
 */
@RestController
public class MessageController {

    private static final Escaper ESCAPER = Escapers.builder()
            .addEscape('"', "\\\"")
            .addEscape('\\', "\\\\")
            .addEscape('\b', "\\b")
            .addEscape('\n', "\\n")
            .addEscape('\t', "\\t")
            .addEscape('\f', "\\f")
            .addEscape('\r', "\\r")
            .build();

    @Resource
    private MessageQuery messageQuery;
    
    @RequestMapping(value = "a", method = RequestMethod.GET)
    public void findByRoomIdAndFromGreaterThan(MessageParameter parameter, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        List<MessageResult> results = messageQuery.findByRoomIdAndFromGreaterThan(parameter);
        PrintWriter buffer = response.getWriter();
        buffer.append('[');
        for(int i = 0, n = results.size(); i < n; i++) {
            MessageResult msg = results.get(i);
            String paramText = msg.getParams().entrySet().stream()
                    .map(paramEntry -> "\"" + paramEntry.getKey() + "\":\"" + translate(paramEntry.getValue()) + "\"")
                    .collect(Collectors.joining(",", "{", "}"));
            buffer
                    .append("{")
                    .append("\"messageId\":").append("\"").append(translate(msg.getMessageId())).append("\"")
                    .append(", ")
                    .append("\"toRoomId\":").append("\"").append(translate(msg.getToRoomId())).append("\"")
                    .append(", ")
                    .append("\"roomId\":").append("\"").append(translate(msg.getRoomId())).append("\"")
                    .append(", ")
                    .append("\"fromUserId\":").append("\"").append(translate(msg.getFromUserId())).append("\"")
                    .append(", ")
                    .append("\"userId\":").append("\"").append(translate(msg.getUserId())).append("\"")
                    .append(", ")
                    .append("\"fromNickname\":").append("\"").append(translate(msg.getFromNickname())).append("\"")
                    .append(", ")
                    .append("\"nickname\":").append("\"").append(translate(msg.getNickname())).append("\"")
                    .append(", ")
                    .append("\"time\":").append(Long.toString(msg.getTime()))
                    .append(", ")
                    .append("\"timeText\":").append("\"").append(translate(msg.getTimeText())).append("\"")
                    .append(", ")
                    .append("\"fromLevel\":").append(Integer.toString(msg.getFromLevel()))
                    .append(", ")
                    .append("\"level\":").append(Integer.toString(msg.getLevel()))
                    .append(", ")
                    .append("\"type\":") .append(Integer.toString(msg.getType()))
                    .append(", ")
                    .append("\"params\":").append(paramText)
                    .append("}");
            if(i < n - 1) {
                buffer.append(',');
            }
        }
        buffer.append(']');
    }

    private static String translate(String input) {
        return ESCAPER.escape(input);
    }

}

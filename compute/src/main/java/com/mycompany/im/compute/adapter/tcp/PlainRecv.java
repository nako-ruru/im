package com.mycompany.im.compute.adapter.tcp;

import com.github.fge.lambdas.runnable.ThrowingRunnable;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mycompany.im.compute.application.ComputeService;
import com.mycompany.im.compute.domain.FromConnectorMessage;
import com.mycompany.im.compute.domain.RoomMsgToCompute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by Administrator on 2017/9/23.
 */
@Component
public class PlainRecv {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ComputeService computeService;

    public void start() {
        Thread t = new Thread((ThrowingRunnable)() -> {
            ServerSocket server = new ServerSocket(22222);
            while(true) {
                try {
                    Socket socket = server.accept();
                    Thread t2 = new Thread(() -> {
                        try {
                            InputStream in = socket.getInputStream();
                            DataInputStream din = new DataInputStream(in);
                            while(true) {
                                int contentLength = din.readInt();
                                byte[] bytes = new byte[contentLength];
                                int read = 0;
                                while((read += in.read(bytes, read, contentLength - read)) < contentLength) {
                                }
                                handle(bytes, 0, contentLength);
                            }
                        } catch (IOException e) {
                            logger.error("", e);
                        } finally {
                            try {
                                if(socket != null) {
                                    socket.close();
                                }
                            } catch (IOException e) {
                                logger.error("", e);
                            }
                        }
                    }, "plain-recv-" + socket.getRemoteSocketAddress());
                    t2.start();
                } catch(IOException e) {
                    logger.error("", e);
                }
            }
        }, "plain-recv-listener");
        t.start();
    }

    private void handle(byte[] bytes, int offset, int length) {
        try {
            bytes = decompress(bytes, offset, length);
            RoomMsgToCompute.FromConnectorMessages fromConnectorMessages = RoomMsgToCompute.FromConnectorMessages.parseFrom(ByteBuffer.wrap(bytes));
            Collection<FromConnectorMessage> messages = fromConnectorMessages.getMessagesList().stream()
                    .flatMap(Stream::of)
                    .map(PlainRecv::newMessage)
                    .collect(Collectors.toCollection(LinkedList::new));
            computeService.compute(messages);
        } catch (InvalidProtocolBufferException | DataFormatException e) {
            logger.error("", e);
        }
    }

    @Resource
    public void setComputeService(ComputeService computeService) {
        this.computeService = computeService;
    }

    private static FromConnectorMessage newMessage(RoomMsgToCompute.FromConnectorMessage m) {
        return new FromConnectorMessage(
                m.getMessageId(),
                m.getRoomId(),
                m.getUserId(),
                m.getNickname(),
                m.getLevel(),
                m.getType(),
                m.getParamsMap(),
                m.getTime()
        );
    }

    public byte[] decompress(byte[] data, int offset, int length) throws DataFormatException {
        Inflater inflater = new Inflater();
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
        try {
            inflater.setInput(data, offset, length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                out.write(buffer, 0, count);
            }
            byte[] output = out.toByteArray();
            logger.debug("Original: " + data.length);
            logger.debug("Compressed: " + output.length);
            return output;
        } finally {
            inflater.end();
            try {
                out.close();
            } catch(IOException e) {
                logger.error("", e);
            }
        }
    }
    
}

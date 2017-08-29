import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by Adminitrator on 2017/7/25.
 */
public class ClientBytesTest {


    @Test
    public void test1() throws IOException {
        String byteText = "0000008a 00000001 7b0a2020 22726f6f 6d496422 203a2022 61363161 32623363 2d323734 352d3438 66662d38 6138302d 66373336 62386439 30616438 222c0a20 2022636f 6e74656e 7422203a 2022e794 b1e4ba8e e8bf87e5 8886222c 0a202022 6e69636b 6e616d65 22203a20 22e4bbb2 e5ad99e5 b5a9e88a 9d222c0a 2020226c 6576656c 22203a20 22313122 0a7d";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for(int i = 0; ;) {
            byte byteBvalue = (byte)Integer.parseInt(byteText.substring(i, i + 2), 16);
            baos.write(byteBvalue);
            i += 2;

            while(i < byteText.length() && byteText.charAt(i) == ' ') {
                i++;
            }
            if(i >= byteText.length()) {
                break;
            }
        }
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        int length = din.readInt();
        int type = din.readInt();
        byte[] jsonBytes = new byte[length];
        din.read(jsonBytes);
        String jsonText = new String(jsonBytes, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        JsonReader jsonReader = gson.newJsonReader(new StringReader(jsonText));
        jsonReader.setLenient(true);
        Message message = gson.fromJson(jsonReader, Message.class);
        System.out.println(message);
    }

    static class Message {

        public String roomId, nickname;
        public int level;
        public String content;

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }

    }


}

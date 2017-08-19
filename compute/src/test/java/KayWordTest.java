import com.mycompany.im.compute.adapter.service.KeywordHandlerImpl;
import com.mycompany.im.compute.domain.KeywordHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Administrator on 2017/7/21.
 */
public class KayWordTest {

    
    @Test
    public void testSpark1() throws InterruptedException {
        KeywordHandler keywordHandler = new KeywordHandlerImpl();

        long start = System.currentTimeMillis();

        for(int i = 0; i < 10000; i++) {
            String result1 = keywordHandler.handle("彩票开奖日期是哪天");
            Assert.assertEquals("彩票****是哪天", result1);

            String result2 = keywordHandler.handle("我是男神");
            Assert.assertEquals("我是男神", result2);
        }

        System.out.println(System.currentTimeMillis() - start);
    }



}

import com.mycompany.im.compute.adapter.service.KeyWorldHandlerImpl;
import com.mycompany.im.compute.domain.KeyWorldHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Administrator on 2017/7/21.
 */
public class KayWordTest {

    
    @Test
    public void testSpark1() throws InterruptedException {
        KeyWorldHandler keyWorldHandler = new KeyWorldHandlerImpl();

        for(int i = 0; i < 10; i++) {
            String result1 = keyWorldHandler.handle("彩票开奖日期是哪天");
            Assert.assertEquals("彩票****是哪天", result1);

            String result2 = keyWorldHandler.handle("我是男神");
            Assert.assertEquals("我是男神", result2);
        }
    }



}

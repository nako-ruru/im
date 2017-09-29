
import com.mycompany.im.naming.spring.mvc.NamingController;
import java.util.Collection;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class NamingTest {
    
    public static void main(String... args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "config.xml", "framework.xml"
        );
        Collection<String> servers = applicationContext.getBean(NamingController.class).servers("go-servers");
        System.err.println(servers);
    }
    
}


import com.mycompany.im.compute.adapter.rpc.IProduct;
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
public class NewClass {
    
    public static void main(String... args) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "dubbo_client.xml"
        );
        IProduct bean = applicationContext.getBean(IProduct.class);
        String productName = bean.getProductName();
        System.out.println(productName);
    }
    
}

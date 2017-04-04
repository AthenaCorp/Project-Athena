package execute;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import test.TestProperties;

public class SpringExecutor {
    public static void main(String[] args) {
        System.out.println("Hello World");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/bean.xml");
        TestProperties properties = (TestProperties) context.getBean("testProperties");
        properties.printProperties();
        context.close();
    }
}

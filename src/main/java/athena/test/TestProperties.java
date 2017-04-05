package athena.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("testProperties")
public class TestProperties {
    @Value("${search.engine.enable.case.fold}")
    private Boolean caseFolding;

    public void printProperties() {
        System.out.println("Case Folding: " + caseFolding);
    }
}

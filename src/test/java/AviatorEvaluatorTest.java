import com.googlecode.aviator.AviatorEvaluator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class AviatorEvaluatorTest {

    @Test
    void AviatorEvaluatorConsole() {

        Map<String, Object> variables = new HashMap<>();
        variables.put("title", "王玮");
        String title;
        try {
            title = AviatorEvaluator.execute("title+'请假'", variables) + "";
        } catch (Exception e) {
            title = "标题加载失败";
        }

        System.out.println(title);
    }
}

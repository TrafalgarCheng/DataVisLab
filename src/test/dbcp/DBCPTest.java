package dbcp;

import com.ssm.demo.service.ArticleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class DBCPTest {

    @Autowired
    private ArticleService articleService;

    @Test
    public void DBCPTest() {
        Long begin = System.currentTimeMillis() / 1000;
        ExecutorService exec = Executors.newFixedThreadPool(100);
        articleService.test(exec);
        exec.shutdown();
        while (true) {
            if (exec.isTerminated()) {
                Long end = System.currentTimeMillis() / 1000;
                Long total = end - begin;
                System.out.println("花费时间:" + total);
                break;
            }
        }
    }
}

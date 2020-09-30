package com.dong.snowflakeplus.temp.test;

import com.dong.snowflakeplus.core.IdProducer;
import com.dong.snowflakeplus.temp.Test1Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @program: snowflake-plus
 * @description
 * @author: DONGSHILEI
 * @create: 2020/9/28 17:43
 **/

@SpringBootTest(classes = Test1Application.class)
@Slf4j
public class ZookeeperHandlerTest {

    @Autowired
    private IdProducer idProducer;

    @Test
    public void snowflake() {

        for (int i=0;i<5;i++) {
            long id = idProducer.id();
            log.info("snowflake:{}",id);
        }
        while (true);
    }
}
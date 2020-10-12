package com.dong.snowflakeplus.temp.test;

import com.dong.snowflakeplus.core.IdProducer;
import com.dong.snowflakeplus.core.strategy.FileStrategy;
import com.dong.snowflakeplus.core.strategy.ISnowflakeStrategy;
import com.dong.snowflakeplus.temp.Test1Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @program: snowflake-plus
 * @description
 * @author: DONGSHILEI
 * @create: 2020/10/12 22:04
 **/
@SpringBootTest(classes = Test1Application.class)
@Slf4j
public class FileStrategyTest {

    private IdProducer idProducer;

    @BeforeEach
    public void init() throws Exception {
        /**
         * snowflake.properties 内容示例
         * workerId=0
         * datacenterId=5
         */
        String filePath = "D:\\app\\snowflake.properties";
        ISnowflakeStrategy snowflakeStrategy = new FileStrategy(filePath);
        idProducer = new IdProducer(snowflakeStrategy);
        idProducer.build();
    }

    @Test
    public void test() throws Exception {
        while (true){
            long id = idProducer.nextId();
            log.info("当前snowflakeId:{}",id);
            Thread.sleep(2000);
        }
    }

}

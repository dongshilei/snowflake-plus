package com.dong.snowflakeplus.temp.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.dong.snowflakeplus.core.IdProducer;
import com.dong.snowflakeplus.core.strategy.ISnowflakeStrategy;
import com.dong.snowflakeplus.core.strategy.ZookeeperStrategy;
import com.dong.snowflakeplus.temp.Test1Application;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.BeforeEach;
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
    private ZooKeeper zkClient;

    private IdProducer idProducer;

    @BeforeEach
    public void init() throws Exception {
        String zkPath = "/snowflake/id";
        ISnowflakeStrategy snowflakeStrategy = new ZookeeperStrategy(zkClient,zkPath,0L,0L);
        idProducer = new IdProducer(snowflakeStrategy);
        idProducer.build();
    }

    /**
     *  测试nextId
     */
    @Test
    public void testNextId() {
        for (int i=0;i<5;i++) {
            long id = idProducer.nextId();
            log.info("当前snowflakeId:{}",id);
        }
        while (true);
    }

    /**
     * 测试advancedId
     * @throws Exception
     */
    @Test
    public void testAdvancedId() throws Exception {
        while (true){
            long id = idProducer.advancedId();
            log.info("当前snowflakeId:{}",id);
            Thread.sleep(2000);
        }
    }

    /**
     * 测试效率
     * 结果 > 4000000/s
     * @throws Exception
     */
    @Test
    public void testEfficiency() throws Exception {
        TimeInterval timer = DateUtil.timer();
        for (int i = 0;i <4000000;i++){
            idProducer.advancedId();
        }
        long interval = timer.interval();
        log.info("生成1000000 id花费时间：{}毫秒",interval);

        Thread.sleep(2000);
    }
}
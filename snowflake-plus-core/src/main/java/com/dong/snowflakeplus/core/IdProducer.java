package com.dong.snowflakeplus.core;


import com.dong.snowflakeplus.core.entity.Snowflake;
import com.dong.snowflakeplus.core.strategy.ZookeeperStrategy;
import com.dong.snowflakeplus.core.worker.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @program: snowflake-plus
 * @description
 * @author: DONGSHILEI
 * @create: 2020/9/28 18:20
 **/
@Component
@Slf4j
public class IdProducer {
    @Autowired
    private ZookeeperStrategy zookeeperStrategy;

    private SnowflakeIdWorker snowflakeIdWorker;

    @PostConstruct
    public void init(){
        Snowflake snowflake = zookeeperStrategy.snowflake();
        snowflakeIdWorker = new SnowflakeIdWorker(snowflake.getWorkerId(),snowflake.getDatacenterId());
    }

    public long id(){
        return snowflakeIdWorker.nextId();
    }

}

package com.dong.snowflakeplus.core;


import com.dong.snowflakeplus.core.entity.Snowflake;
import com.dong.snowflakeplus.core.strategy.ISnowflakeStrategy;
import com.dong.snowflakeplus.core.strategy.ZookeeperStrategy;
import com.dong.snowflakeplus.core.worker.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;

/**
 * @program: snowflake-plus
 * @description
 * @author: DONGSHILEI
 * @create: 2020/9/28 18:20
 **/
@Slf4j
public class IdProducer {

    private ISnowflakeStrategy snowflakeStrategy;

    private SnowflakeIdWorker snowflakeIdWorker;

    public IdProducer(ISnowflakeStrategy snowflakeStrategy) {
        this.snowflakeStrategy = snowflakeStrategy;
    }

    /**
     * 初始化snowflakeIdWorker，建议在IdProducer构建后调用一次
     *
     * @throws Exception
     */
    public void build() throws Exception {
        Snowflake snowflake = getSnowflake();
        snowflakeIdWorker = new SnowflakeIdWorker(snowflake.getWorkerId(), snowflake.getDatacenterId());
    }

    /**
     * 获取ID
     * 当发生系统时钟回拨后，如果使用了zookeeper策略，系统自动调整Snowflake
     * @return
     * @throws Exception
     */
    public synchronized long nextId() throws Exception {
        try {
             return snowflakeIdWorker.nextId();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("backwards")
                    &&snowflakeStrategy instanceof ZookeeperStrategy){
                // 发生了时钟回拨 且使用了zookeeper策略
                log.warn(e.getMessage());
                //重置 snowflake
                build();
            } else {
                throw  e;
            }
        }
        return snowflakeIdWorker.nextId();
    }


    /**
     * 支持单独获取Snowflake，建议在IdProducer构建后调用一次
     *
     * @return
     * @throws Exception
     */
    public Snowflake getSnowflake() throws Exception {
        Snowflake snowflake = snowflakeStrategy.snowflake();
        log.info("snowflake:【{}】", snowflake.toString());
        return snowflake;
    }

}

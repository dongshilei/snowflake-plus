package com.dong.snowflakeplus.core;


import com.dong.snowflakeplus.core.entity.Snowflake;
import com.dong.snowflakeplus.core.strategy.ISnowflakeStrategy;
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

    public static Long snowflakeId = 0L;

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

    public long nextId() {
        return snowflakeIdWorker.nextId();
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     * 高级的ID，当发生系统时钟回拨后，自动调整Snowflake
     *
     * @return
     */
    public synchronized long advancedId() throws Exception {
        if (snowflakeId == 0) {
            //首次使用，不需要判断是否回拨
            snowflakeId = snowflakeIdWorker.nextId();
        } else {
            long nextId = snowflakeIdWorker.nextId();
            if (nextId < snowflakeId) {
                // 有可能发生了时钟回拨
                log.warn("有可能发生了时钟回拨");
                build();
                snowflakeId = snowflakeIdWorker.nextId();
            } else {
                snowflakeId = nextId;
            }
        }
        return snowflakeId;
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

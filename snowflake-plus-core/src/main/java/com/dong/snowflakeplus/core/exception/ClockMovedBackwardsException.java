package com.dong.snowflakeplus.core.exception;

/**
 * 时钟倒退异常
 * @program: snowflake-plus
 * @description
 * @author: dongshilei
 * @create: 2021/6/24 16:14
 **/
public class ClockMovedBackwardsException extends RuntimeException{

    private long lastTimestamp = -1L;
    private long timestamp = -1L;

    public ClockMovedBackwardsException(long lastTimestamp,long timestamp) {
        super(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
    }
}

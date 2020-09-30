package com.dong.snowflakeplus.core.strategy;


import com.dong.snowflakeplus.core.entity.Snowflake;

/**
 * @program: snowflake-plus
 * @description
 * @author: DONGSHILEI
 * @create: 2020/9/28 18:14
 **/
public interface ISnowflakeStrategy {
    Snowflake snowflake() throws Exception;
}

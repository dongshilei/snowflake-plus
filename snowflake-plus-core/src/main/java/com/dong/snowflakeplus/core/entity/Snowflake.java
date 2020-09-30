package com.dong.snowflakeplus.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: snowflake-plus
 * @description 雪花实体
 * @author: DONGSHILEI
 * @create: 2020/9/25 22:55
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Snowflake {

    /** 工作机器ID(0~31) */
    private long workerId;

    /** 数据中心ID(0~31) */
    private long datacenterId;
}

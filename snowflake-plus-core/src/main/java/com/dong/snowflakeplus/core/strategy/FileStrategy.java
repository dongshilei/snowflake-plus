package com.dong.snowflakeplus.core.strategy;

import com.dong.snowflakeplus.core.entity.Snowflake;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @program: snowflake-plus
 * @description 读取Properties配置文件,组装Snowflake
 *  无法避免时钟回拨
 * @author: DONGSHILEI
 * @create: 2020/10/12 21:48
 **/
public class FileStrategy implements ISnowflakeStrategy {
    /**
     * 配置文件路径
     */
    private String filePath;

    public FileStrategy(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Snowflake snowflake() throws Exception {
        if (StringUtils.isBlank(filePath)) {
            throw new RuntimeException("Properties file does not exist");
        }
        InputStream in = new BufferedInputStream(new FileInputStream(filePath));
        Properties p = new Properties();
        p.load(in);
        String datacenterId = p.getProperty("datacenterId");
        if (StringUtils.isBlank(datacenterId)) {
            throw new RuntimeException("datacenterId is null");
        }
        String workerId = p.getProperty("workerId");
        if (StringUtils.isBlank(workerId)) {
            throw new RuntimeException("workerId is null");
        }
        return new Snowflake(Long.valueOf(workerId), Long.valueOf(datacenterId));
    }
}

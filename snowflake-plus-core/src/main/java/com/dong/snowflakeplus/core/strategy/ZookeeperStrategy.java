package com.dong.snowflakeplus.core.strategy;

import com.dong.snowflakeplus.core.entity.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @program: snowflake-plus
 * @description
 * @author: DONGSHILEI
 * @create: 2020/9/25 22:46
 **/
@Component
@Slf4j
public class ZookeeperStrategy  implements ISnowflakeStrategy{
    @Autowired
    private ZooKeeper zkClient;
    @Value("${snowflake-plus.zk.path}")
    private String zkPath;
    @Value("${snowflake-plus.snowflake.workerId}")
    private long workerId = 0L;
    @Value("${snowflake-plus.snowflake.datacenterId}")
    private long datacenterId = 0L;

    private Snowflake snowflake;

    @PostConstruct
    private void init() throws Exception {
        snowflake = new Snowflake(workerId, datacenterId);
        //加工处理zkPath
        processZkPath();
        //初始化zkPath
        initCustomNode();
    }

    @Override
    public Snowflake snowflake() {
        try {
            boolean createResult;
            do {
                String ip = InetAddress.getLocalHost().getHostAddress();
                // 创建临时顺序节点
                String workerPath = zkPath.concat("/").concat(String.valueOf(snowflake.getDatacenterId()))
                        .concat("/").concat(String.valueOf(snowflake.getWorkerId()));
                String workerNode = zkClient.create(workerPath, ip.getBytes(), OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                log.info("获取临时workerNode:{}", workerNode);
                snowflake = updateSnowflake(workerNode);
                //创建临时节点
                String layer = zkPath.concat("/").concat(String.valueOf(snowflake.getDatacenterId()))
                        .concat("/").concat(String.valueOf(snowflake.getWorkerId()));
                // 当createResult：true时，表示当前WorkerId可用；否则不可用，需要继续试下一个节点
                createResult = createEphemeralNode(layer);
            } while (!createResult);
        } catch (Exception e) {
            log.error("获取snowflake异常", e);
        }
        return snowflake;
    }

    /**
     * 创建 datacenterId 节点
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    private void initCustomNode() throws Exception {
        String[] nodes = zkPath.split("/");
        StringBuffer layer = new StringBuffer("/");
        //循环创建自定义持久节点
        for (int i = 0; i < nodes.length; i++) {
            if (StringUtils.isNotBlank(nodes[i])) {
                layer.append(nodes[i]);
                createPersistentNode(layer.toString());
                layer.append("/");
            }
        }
        //初始化数据中心节点
        createDatacenterNode();
    }

    /**
     * 在自定义节点下创建数据中心节点（持久节点）
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    private void createDatacenterNode() throws Exception {
        String layer = zkPath.concat("/").concat(String.valueOf(snowflake.getDatacenterId()));
        createPersistentNode(layer);
    }

    /**
     * 创建永久节点
     *
     * @param layer
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    private void createPersistentNode(String layer) throws Exception {
        log.info("createPersistentNode:{}", layer);
        try {
            byte[] bytes = new byte[0];
            //创建持久节点
            String nodePath = zkClient.create(layer, bytes, OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            log.info("nodePath:{}", nodePath);
        } catch (KeeperException e) {
            if (e.getMessage().contains("NodeExists")) {
                log.info("{}已存在", layer);
            } else {
                throw e;
            }
        } catch (InterruptedException e) {
            throw e;
        }
    }

    /**
     * 创建临时节点
     * true:新创建成功
     * false: 已存在|创建失败
     *
     * @param layer
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    private boolean createEphemeralNode(String layer) throws Exception {
        log.info("createNode:{}", layer);
        boolean result = true;
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String nodePath = zkClient.create(layer, ip.getBytes(), OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            log.info("nodePath:{}", nodePath);
        } catch (KeeperException e) {
            if (e.getMessage().contains("NodeExists")) {
                log.info("{}已存在", layer);
                result = false;
            } else {
                throw e;
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (UnknownHostException e) {
            throw e;
        }
        return result;
    }

    /**
     * 更新当前 snowflake
     *
     * @param workerPath
     * @return
     */
    private Snowflake updateSnowflake(String workerPath) {
        Snowflake snowflake = new Snowflake();
        if (workerPath != null && !workerPath.equals("")) {
            String[] split = workerPath.split("/");
            String datacenterStr = split[split.length - 2];
            String workerStr = split[split.length - 1];
            int datacenterId = Integer.parseInt(datacenterStr) & 31;
            int workerId = Integer.parseInt(workerStr) & 31;
            snowflake.setDatacenterId(datacenterId);
            snowflake.setWorkerId(workerId);
        }
        return snowflake;
    }

    /**
     * 加工处理zkPath
     * 去掉zkPath的 / 结尾
     */
    private void processZkPath() {
        if (zkPath.endsWith("/")) {
            zkPath = zkPath.substring(0, zkPath.length() - 2);
        }
    }
}

package com.dong.snowflakeplus.core.strategy;

import com.dong.snowflakeplus.core.entity.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @program: snowflake-plus
 * @description
 * @author: DONGSHILEI
 * @create: 2020/9/25 22:46
 **/
@Slf4j
public class ZookeeperStrategy  implements ISnowflakeStrategy{

    private ZooKeeper zkClient;
    /**自定义zk节点的路径，用于创建snowflake-plus节点*/
    private String zkPath;
    /**工作机器ID(0~31)  默认为0*/
    private long workerId = 0L;
    /**数据中心ID(0~31)  默认为0*/
    private long datacenterId = 0L;

    private final int FACTOR = 31;

    private String DATACENTER = "datacenter_";
    /**现役的*/
    private String WORKER_ACTIVE = "active";
    /**顺序节点*/
    private String WORKER_INDEX = "index";

    public static Snowflake snowflake;

    public static String dataCenterPath;

    public static String workerActivePath;

    public static String workerIndexPath;

    public ZookeeperStrategy(ZooKeeper zkClient, String zkPath, long workerId, long datacenterId) throws Exception {
        this.zkClient = zkClient;
        this.zkPath = zkPath;
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    @Override
    public Snowflake snowflake() throws Exception {
        snowflake = new Snowflake(workerId, datacenterId);
        //加工处理zkPath
        processZkPath();
        //初始化dataCenterPath
        initDataCenterPath();
        try {
            boolean createResult;
            do {
                String ip = InetAddress.getLocalHost().getHostAddress();
                // 创建临时顺序节点
                String workerIndexPath = dataCenterPath.concat("/").concat(WORKER_INDEX).concat("/");
                String workerNode = zkClient.create(workerIndexPath, ip.getBytes(), OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                log.info("获取临时workerNode:{}", workerNode);
                snowflake = updateSnowflake(workerNode);
                //创建临时节点
                String layer = dataCenterPath.concat("/").concat(WORKER_ACTIVE).concat("/").concat(String.valueOf(snowflake.getWorkerId()));
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
    private void initDataCenterPath() throws Exception {
        dataCenterPath = zkPath.concat("/").concat(DATACENTER).concat(String.valueOf(snowflake.getDatacenterId()));
        String[] nodes = dataCenterPath.split("/");
        StringBuffer layer = new StringBuffer("/");
        //循环创建自定义持久节点
        for (int i = 0; i < nodes.length; i++) {
            if (StringUtils.isNotBlank(nodes[i])) {
                layer.append(nodes[i]);
                createPersistentNode(layer.toString());
                layer.append("/");
            }
        }
        // 创建现役节点目录
        createPersistentNode(dataCenterPath.concat("/").concat(WORKER_ACTIVE));
        // 创建顺序节点目录
        createPersistentNode(dataCenterPath.concat("/").concat(WORKER_INDEX));
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
        log.info("createEphemeralNode:{}", layer);
        boolean result = true;
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String nodePath = zkClient.create(layer, ip.getBytes(), OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            log.info("EphemeralNode Path:{}", nodePath);
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
            String datacenterStr = split[split.length - 3];
            String workerStr = split[split.length - 1];
            datacenterStr = datacenterStr.substring(datacenterStr.indexOf("_")+1);
            int datacenterId = Integer.parseInt(datacenterStr) & FACTOR;
            int workerId = Integer.parseInt(workerStr) & FACTOR;
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

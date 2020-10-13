# snowflake-plus
使用zookeeper特性，创建SnowflakeIdWorker

初衷：Twitter_Snowflake是个不错的分布式ID生成器，只需要配置workerId和datacenterId即可使用（相同服务不同机器配置的workerId不同）。通常会将workerId和datacenterId写入单独的配置文件。
但是在实际应用中，经常出现忘记配置文件，或者配置文件中的信息相同等问题，造成ID重复等问题。
为了方便使用，同时避免不同服务器配置的workerId相同的问题，snowflake-plus可以作为一种方案。

配置文件优缺点

优点：简单，无需依赖其他服务

缺点：

1、容易遗漏放置配置文件到服务器

2、易发生配置文件重复，导致ID重复

使用zookeeper优化雪花算法ID

设计思路：

1、利用zookeeper临时有序节点的特性（有序，客户端断开后自动删除），应用启动时创建临时有序节点

2、将临时有序节点转换为整数，并对32取模，得到值作为备用workerId

3、创建名称为workerId值的临时节点，如果创建成功，则表示workerId可用;如果创建失败，则表示workerId值已被占用，不可用，需要重新获取临时有序节点，重复步骤2，3

4、使用workerId和datacenterId构造SnowflakeIdWorker，可保证不同服务器之间产生的ID不同

5、应用停掉后，断开与zookeeper的连接，临时有序节点和临时节点被清除，又可以被后续应用使用。

6、当发生时钟回拨时，为防止ID重复，自动调整workerId，使用新的SnowflakeIdWorker来生成ID

ID生成效率  大于 4000000个/s



后续规划：
1、datacenterId（0~31）可以循环使用


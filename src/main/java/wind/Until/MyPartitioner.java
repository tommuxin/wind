package wind.Until;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MyPartitioner  implements Partitioner {
    private final ConcurrentMap<String, AtomicInteger> topicCounterMap = new ConcurrentHashMap();
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        /* 首先通过cluster从元数据中获取topic所有的分区信息 */
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        //拿到该topic的分区数
        int numPartitions = partitions.size();
        //如果消息记录中没有指定key
        if (key.toString() == null) {
            //则获取一个自增的值
            int nextValue = nextValue(topic);
            //通过cluster拿到所有可用的分区（可用的分区这里指的是该分区存在首领副本）
            List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
            //如果该topic存在可用的分区
            if (availablePartitions.size() > 0) {
                //那么将nextValue转成正数之后对可用分区数进行取余
                int part = Utils.toPositive(nextValue) % availablePartitions.size();
                //然后从可用分区中返回一个分区
                return availablePartitions.get(part).partition();
            } else { // 如果不存在可用的分区
                //那么就从所有不可用的分区中通过取余的方式返回一个不可用的分区
                return Utils.toPositive(nextValue) % numPartitions;
            }
        } else { // 如果消息记录中指定了key
            // 则使用该key进行hash操作，然后对所有的分区数进行取余操作，这里的hash算法采用的是murmur2算法，然后再转成正数
            //toPositive方法很简单，直接将给定的参数与0X7FFFFFFF进行逻辑与操作。
            return Integer.parseInt((String) key) % numPartitions;
        }
    }

    private int nextValue(String topic) {
        AtomicInteger counter = (AtomicInteger) this.topicCounterMap.get(topic);
        if (null == counter) {
            counter = new AtomicInteger((new Random()).nextInt());
            AtomicInteger currentCounter = (AtomicInteger) this.topicCounterMap.putIfAbsent(topic, counter);
            if (currentCounter != null) {
                counter = currentCounter;
            }
        }

        return counter.getAndIncrement();
    }


    public void close() {
    }

    public void configure(Map<String, ?> configs) {
    }
}





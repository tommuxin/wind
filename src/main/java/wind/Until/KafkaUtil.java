package wind.Until;


import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.requests.MetadataResponse;
import org.apache.kafka.common.utils.Utils;
import wind.Until.ConfigUntil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


public class KafkaUtil {
    private static Properties kafka_properties;
    private static Producer<String, String> producer;
    private static KafkaConsumer<String, String> consumer;
    private static ConsumerRecords<String, String> msgList;

    static {
        kafka_properties = new Properties();
        // 指定bootstrap.servers属性。必填，无默认值。用于创建向kafka broker服务器的连接。
        kafka_properties.put("bootstrap.servers", ConfigUntil.getConfig("kafka.hosts"));
        // 指定key.serializer属性。必填，无默认值。被发送到broker端的任何消息的格式都必须是字节数组。
        // 因此消息的各个组件都必须首先做序列化，然后才能发送到broker。该参数就是为消息的key做序列化只用的。
        kafka_properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 指定value.serializer属性。必填，无默认值。和key.serializer类似。此被用来对消息体即消息value部分做序列化。
        // 将消息value部分转换成字节数组。
        kafka_properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafka_properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafka_properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        //acks参数用于控制producer生产消息的持久性（durability）。参数可选值，0、1、-1（all）。
        kafka_properties.put("acks", "-1");
        //props.put(ProducerConfig.ACKS_CONFIG, "1");
        //在producer内部自动实现了消息重新发送。默认值0代表不进行重试。
        kafka_properties.put("retries", 3);
        //props.put(ProducerConfig.RETRIES_CONFIG, 3);
        //调优producer吞吐量和延时性能指标都有非常重要作用。默认值16384即16KB。
        kafka_properties.put("batch.size", 323840);
        //props.put(ProducerConfig.BATCH_SIZE_CONFIG, 323840);
        //控制消息发送延时行为的，该参数默认值是0。表示消息需要被立即发送，无须关系batch是否被填满。
        kafka_properties.put("linger.ms", 10);
        //props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        //指定了producer端用于缓存消息的缓冲区的大小，单位是字节，默认值是33554432即32M。
        kafka_properties.put("buffer.memory", 33554432);
        //props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        kafka_properties.put("max.block.ms", 3000);
        kafka_properties.put("partitioner.class", "wind.Until.MyPartitioner");
        //是否设置自动提交
        // kafka_properties.put("enable.auto.commit", "false");
        //props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);
        //设置producer段是否压缩消息，默认值是none。即不压缩消息。GZIP、Snappy、LZ4
        //props.put("compression.type", "none");
        //props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
        //该参数用于控制producer发送请求的大小。producer端能够发送的最大消息大小。
        //props.put("max.request.size", 10485760);
        //props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10485760);
        //producer发送请求给broker后，broker需要在规定时间范围内将处理结果返还给producer。默认30s
        //props.put("request.timeout.ms", 60000);
        //props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);


        // 使用上面创建的Properties对象构造KafkaProducer对象
        //如果采用这种方式创建producer，那么就不需要显示的在Properties中指定key和value序列化类了呢。
        // Serializer<String> keySerializer = new StringSerializer();
        // Serializer<String> valueSerializer = new StringSerializer();
        // Producer<String, String> producer = new KafkaProducer<String, String>(props,
        // keySerializer, valueSerializer);
    }

    /**
     * 插入topic消息
     * topic_name 指定要插入的topic名字
     * list_re 插入topic 的数据集合
     */


    public void insertTopic(String topic_name, ArrayList list_re) {
        producer = new KafkaProducer<>(kafka_properties);

        for (int i = 0; i < list_re.size(); i++) {
            //构造好kafkaProducer实例以后，下一步就是构造消息实例。
            producer.send(new ProducerRecord<>(topic_name, "1", list_re.get(i).toString()));

            // 构造待发送的消息对象ProduceRecord的对象，指定消息要发送到的topic主题，分区以及对应的key和value键值对。Integer.toString(i)
            // 注意，分区和key信息可以不用指定，由kafka自行确定目标分区。
            //ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>("my-topic",
            //        Integer.toString(i), Integer.toString(i));
            // 调用kafkaProduce的send方法发送消息
            //producer.send(producerRecord);
        }
        System.out.println("消息生产结束......");
        // 关闭kafkaProduce对象
        producer.close();
        System.out.println("关闭生产者......");

    }

    public void insertTopic(String topic_name, String list_re) {
        producer = new KafkaProducer<>(kafka_properties);
        //构造好kafkaProducer实例以后，下一步就是构造消息实例。
        producer.send(new ProducerRecord<>(topic_name, "1", list_re));

        // 构造待发送的消息对象ProduceRecord的对象，指定消息要发送到的topic主题，分区以及对应的key和value键值对。Integer.toString(i)
        // 注意，分区和key信息可以不用指定，由kafka自行确定目标分区。
        //ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>("my-topic",
        //        Integer.toString(i), Integer.toString(i));
        // 调用kafkaProduce的send方法发送消息
        //producer.send(producerRecord);

        System.out.println("消息生产结束......");
        // 关闭kafkaProduce对象
        producer.close();
        System.out.println("关闭生产者......");

    }

    /**
     * 插入topic消息时指定partition插入
     * topic_name 指定要插入的topic名字
     * list_re 插入topic 的数据集合
     * partion_id 需要插入的分区id
     */
    public void insertTopic(String topic_name, String partion_id, ArrayList list_re) {

        producer = new KafkaProducer<>(kafka_properties);

        for (int i = 0; i < list_re.size(); i++) {
            //构造好kafkaProducer实例以后，下一步就是构造消息实例。
            producer.send(new ProducerRecord<String, String>(topic_name, partion_id, list_re.get(i).toString()));
        }
        System.out.println("消息生产结束......");
        // 关闭kafkaProduce对象
        producer.close();

        System.out.println("关闭生产者......");

    }

    public int insertTopic(String topic_name, String partion_id, String[] list_re) {

        producer = new KafkaProducer<>(kafka_properties);
        int sucess = 0;
        for (int i = 0; i < list_re.length; i++) {
            //构造好kafkaProducer实例以后，下一步就是构造消息实例。
            producer.send(new ProducerRecord<String, String>(topic_name, partion_id, list_re[i].toString()));
            sucess = 1;
        }
        System.out.println("消息生产结束......");
        // 关闭kafkaProduce对象
        producer.close();

        System.out.println("关闭生产者......");
        return sucess;
    }

    /**
     * 全量消费topic消息
     * topic_name 指定要消费的topic名字
     * group_id  topic 分组id
     * group_name topic 分组名称
     * auto_offset 读取方式
     */

    public ArrayList<String> cousumertopic(String topic_name, String group_id, String group_name, String auto_offset) {
        kafka_properties.put("group.id", group_id);
        kafka_properties.put("group.name", group_name);
        kafka_properties.put("enable.auto.commit", "true");
        kafka_properties.put("auto.commit.interval.ms", "1000");
        kafka_properties.put("session.timeout.ms", "30000");
        kafka_properties.put("auto.offset.reset", auto_offset);
        kafka_properties.put("max.poll.interval.ms", "500");
        kafka_properties.put("max.poll.records", "50");
        ArrayList<String> result = new ArrayList<String>();
        this.consumer = new KafkaConsumer<String, String>(kafka_properties);

        this.consumer.subscribe(Arrays.asList(topic_name));//消费topic
        msgList = consumer.poll(1000);

        // for (;;) {
        if (null != msgList && msgList.count() > 0) {
            for (ConsumerRecord<String, String> record : msgList) {
                //消费100条就打印 ,但打印的数据不一定是这个规律的
                // if(messageNo%100==0){
                result.add(record.value());
                // System.out.println("partition =" + record.partition() + ",key =" + record.key() + ", value = " + record.value() + " offset===" + record.offset());
                //}
                //当消费了1000条就退出
                // if(messageNo%1000==0){
                //    break;
                //  }
                // messageNo++;
            }
        }//else{
        //     Thread.sleep(1000);

        return result;
        //  }

    }

    /**
     * 分区消费topic消息
     * topic_name 指定要消费的topic名字
     * partition_id 分区编号
     * group_id  topic 分组id
     * group_name topic 分组名称
     * auto_offset 读取方式
     */

    public ArrayList<String> cousumertopic(String topic_name, int partition_id, String group_id, String group_name, String auto_offset) {
        kafka_properties.put("group.id", group_id);
        kafka_properties.put("group.name", group_name);
        kafka_properties.put("enable.auto.commit", "true");
        kafka_properties.put("auto.commit.interval.ms", "1000");
        kafka_properties.put("session.timeout.ms", "30000");
        kafka_properties.put("auto.offset.reset", auto_offset);
        kafka_properties.put("max.poll.interval.ms", "500");
        kafka_properties.put("max.poll.records", "50");
        ArrayList<String> result = new ArrayList<String>();
        this.consumer = new KafkaConsumer<String, String>(kafka_properties);
        TopicPartition p = new TopicPartition(topic_name, partition_id);//消费某partition
        this.consumer.assign(Arrays.asList(p));

        msgList = consumer.poll(1000);
        // for (;;) {
        if (null != msgList && msgList.count() > 0) {
            for (ConsumerRecord<String, String> record : msgList) {
                //消费100条就打印 ,但打印的数据不一定是这个规律的
                // if(messageNo%100==0){
                result.add(record.value());
                // System.out.println("partition =" + record.partition() + ",key =" + record.key() + ", value = " + record.value() + " offset===" + record.offset());
                //}
                //当消费了1000条就退出
                // if(messageNo%1000==0){
                //    break;
                //  }
                // messageNo++;
            }
        }//else{
        //     Thread.sleep(1000);

        return result;
        //  }

    }

    public void createTopics(String topic_name, int partions, int replication) throws ExecutionException, InterruptedException {
        NewTopic newTopic = new NewTopic(topic_name, partions, (short) replication);

        Collection<NewTopic> newTopicList = new ArrayList<>();
        newTopicList.add(newTopic);
        AdminClient adminClient = AdminClient.create(kafka_properties);
        CreateTopicsResult topics = adminClient.createTopics(newTopicList);
        topics.all().get();
        adminClient.close();
    }


    public void deleteTopics(String topic_name) throws ExecutionException, InterruptedException {
        AdminClient adminClient = AdminClient.create(kafka_properties);
        Collection<String> newTopicList1 = new ArrayList<>();
        newTopicList1.add(topic_name);
        DeleteTopicsResult topics = adminClient.deleteTopics(newTopicList1);
        topics.all().get();
        adminClient.close();
    }

    public void listtopics() throws ExecutionException, InterruptedException {
        AdminClient adminClient = AdminClient.create(kafka_properties);
        ListTopicsResult res = adminClient.listTopics();
        Set<String> res1 = res.names().get();
        for (String tt : res1) {
            System.out.println(tt);
        }
        adminClient.close();
    }

    public void describetopics(String topic_name) throws ExecutionException, InterruptedException {
        int COUNT = 30;
        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafka_properties);
        AdminClient adminClient = AdminClient.create(kafka_properties);
        Collection<String> newTopicList1 = new ArrayList<>();
        newTopicList1.add(topic_name);
        DescribeTopicsResult descriresult = adminClient.describeTopics(newTopicList1);
        Map<String, KafkaFuture<TopicDescription>> descriresultva = descriresult.values();
        Iterator<Map.Entry<String, KafkaFuture<TopicDescription>>> itr = descriresultva.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, KafkaFuture<TopicDescription>> entry = itr.next();
            System.out.println("key: " + entry.getKey());
            List<TopicPartitionInfo> topicPartitionInfoList = entry.getValue().get().partitions();
            topicPartitionInfoList.forEach((e) -> {
                int partitionId = e.partition();
                Node node = e.leader();
                TopicPartition topicPartition = new TopicPartition(topic_name, partitionId);
                Map<TopicPartition, Long> mapBeginning = consumer.beginningOffsets(Arrays.asList(topicPartition));
                Iterator<Map.Entry<TopicPartition, Long>> itr2 = mapBeginning.entrySet().iterator();
                long beginOffset = 0;
                //mapBeginning只有一个元素，因为Arrays.asList(topicPartition)只有一个topicPartition
                while (itr2.hasNext()) {
                    Map.Entry<TopicPartition, Long> tmpEntry = itr2.next();
                    beginOffset = tmpEntry.getValue();
                }
                Map<TopicPartition, Long> mapEnd = consumer.endOffsets(Arrays.asList(topicPartition));
                Iterator<Map.Entry<TopicPartition, Long>> itr3 = mapEnd.entrySet().iterator();
                long lastOffset = 0;
                while (itr3.hasNext()) {
                    Map.Entry<TopicPartition, Long> tmpEntry2 = itr3.next();
                    lastOffset = tmpEntry2.getValue();
                }
                long expectedOffSet = lastOffset - COUNT;
                expectedOffSet = expectedOffSet > 0 ? expectedOffSet : 1;
                System.out.println("Leader of partitionId: " + partitionId + "  is " + node + ".  expectedOffSet:" + expectedOffSet
                        + "，  beginOffset:" + beginOffset + ", lastOffset:" + lastOffset);
                consumer.commitSync(Collections.singletonMap(topicPartition, new OffsetAndMetadata(expectedOffSet - 1)));
            });

        }

        adminClient.close();
        consumer.close();
    }

    /**
     * 获取当前topic下的某个分区的偏移量信息
     *
     * @param partion_id
     * @return offset
     */


    public static long getPartitionsOffset(String topic_name, int partion_id) throws ExecutionException, InterruptedException {
        long lastOffset = 0;
        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafka_properties);
        AdminClient adminClient = AdminClient.create(kafka_properties);
        Collection<String> newTopicList1 = new ArrayList<>();
        newTopicList1.add(topic_name);
        DescribeTopicsResult descriresult = adminClient.describeTopics(newTopicList1);
        Map<String, KafkaFuture<TopicDescription>> descriresultva = descriresult.values();
        Iterator<Map.Entry<String, KafkaFuture<TopicDescription>>> itr = descriresultva.entrySet().iterator();
        while (itr.hasNext()) {

            TopicPartition topicPartition = new TopicPartition(topic_name, partion_id);
            Map<TopicPartition, Long> mapEnd = consumer.endOffsets(Arrays.asList(topicPartition));

            Iterator<Map.Entry<TopicPartition, Long>> itr3 = mapEnd.entrySet().iterator();

            while (itr3.hasNext()) {
                Map.Entry<TopicPartition, Long> tmpEntry2 = itr3.next();
                lastOffset = tmpEntry2.getValue();
            }
            System.out.println("Leader of partitionId: " + partion_id + " lastOffset:" + lastOffset);


        }

        adminClient.close();
        consumer.close();
        return lastOffset;
    }


}
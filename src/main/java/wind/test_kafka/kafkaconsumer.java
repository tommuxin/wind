package wind.test_kafka;

import org.apache.kafka.common.TopicPartition;
import wind.Until.ConfigUntil;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;


public class kafkaconsumer implements Runnable {
    /**
     * Title: KafkaConsumerTest
     * Description:
     * kafka消费者 demo
     * Version:1.0.0
     *
     * @author pancm
     * @date 2018年1月26日
     */


    private final KafkaConsumer<String, String> consumer;
    private ConsumerRecords<String, String> msgList;
    private final String topic;

    public kafkaconsumer(String topicName,String group_id) {
        Properties props = new Properties();
        props.put("bootstrap.servers", ConfigUntil.getConfig("kafka.hosts"));
        props.put("group.id", group_id);
        props.put("group.name", 1);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");

        // 必须指定
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // 必须指定
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        this.consumer = new KafkaConsumer<String, String>(props);
        this.topic = topicName;
        TopicPartition p = new TopicPartition(this.topic, 2);//消费某partition
        this.consumer.assign(Arrays.asList(p));
     /*      this.consumer.subscribe(Arrays.asList(topic));//消费topic
     TopicPartition p = new TopicPartition("test6", 2);//只消费分区号为2的分区
        consumer.assign(Arrays.asList(p));//只消费分区号为2的分区
        consumer.subscribe(Arrays.asList("test6"));//消费topic 消费全部分区
        consumer.seekToBeginning(Arrays.asList(p));//重头开始消费
        consumer.seek(p,5);//指定从topic的分区的某个offset开始消费*/

    }

    @Override
    public void run() {
        int messageNo = 1;
        System.out.println("---------开始消费---------");
        try {
            for (; ; ) {
                msgList = consumer.poll(1000);
                if (null != msgList && msgList.count() > 0) {
                    for (ConsumerRecord<String, String> record : msgList) {
                        //消费100条就打印 ,但打印的数据不一定是这个规律的
                        // if(messageNo%100==0){
                        System.out.println(messageNo + ", partition ="+record.partition()+",key ="+ record.key() + ", value = " + record.value() + " offset===" + record.offset());
                        //}
                        //当消费了1000条就退出
                        // if(messageNo%1000==0){
                        //    break;
                        //  }
                        // messageNo++;
                    }
                }//else{
                //     Thread.sleep(1000);
                //  }
            }
        } finally {
            consumer.close();
        }
    }

    public static void main(String args[]){
        kafkaconsumer yy=new kafkaconsumer("test1","ad");
        Thread uu= new Thread(yy);
        uu.start();
    }

}

package wind.test_kafka;

import wind.Until.KafkaUtil;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class test_kafka {
    ArrayList<String> ty = new ArrayList<String>();

    public ArrayList<String> coll(int tol) {
        String i_y = "tom,rrrr";
        for (int i = 0; i < tol; i++) {
            ty.add(i_y + ",222," + i);
        }
        return ty;
    }

    public static void main(String args[]) throws ExecutionException, InterruptedException {

        test_kafka yy2 = new test_kafka();
        KafkaUtil kaf = new KafkaUtil();
        /*
            kafkaconsumer test2 = new kafkaconsumer(ConfigUntil.getConfig("topic.test1"),"group_id");
            Thread thread1 = new Thread(test2);
            thread1.start();*/

        //ArrayList<String> cc = yy2.coll(1000);


kaf.deleteTopics("test100");
      //kaf.createTopics("test100",3,1);
        //-1 =0 0=2
        //kaf.insertTopic("test1",cc);
    //kaf.insertTopic("test1","1", "nihao nimei".split(" "));
       // kaf.insertTopic("test1",cc);
 /*  ArrayList rt=kaf.cousumertopic("test10","1","nimei","earliest");
        for (int r=0;r<rt.size();r++)
        {
            System.out.println(rt.get(r));
        }*/
        //kaf.describetopics("test3");
 //long tt= kaf.getPartitionsOffset("test1",2);





          //   System.out.println(tt);

//kaf.insert_Topic_1("test1","rrrrrrrrrrrrr");

    }
}

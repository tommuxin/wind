package wind.test_hbase;


import wind.Until.HbaseUntil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class TestHbase {

    ArrayList<String> ty = new ArrayList<String>();

    public ArrayList<String> coll(int tol) {
        String i_y = "tom";
        for (int i = 0; i < tol; i++) {
            ty.add(i_y + "," + i);
        }
        return ty;
    }

    public static void main(String args[]) throws ExecutionException, InterruptedException, IOException {
        HbaseUntil yy1 = new HbaseUntil();
        TestHbase yy2 = new TestHbase();
        ArrayList<String> cc = yy2.coll(1000);
        yy1.createTable("table1", "t1");
        String[] name1 = new String[2];
        name1[0] = "name";
        name1[1] = "age";
        yy1.insertRecordss("table1", "ab", 100, "t1", name1, cc);
        // yy1.deleteRow("table1", "ab99");
        //String re = yy1.selectValue("table1", "a", "t1", "age");

    }
}

package wind.Until;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Feng on 2020/1/13 11:35
 * CurrentProject's name is hbase-demo<br>
 * Hbase API 简单封装
 *
 * @author Feng
 */
public class HbaseUntil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HbaseUntil.class);
    private static final HbaseUntil INSTANCE = new HbaseUntil();
    private static volatile Connection hbase_connection = null;

    private static volatile Admin admin = null;

    /*
     * 初始化数据：配置信息；获取 connection 对象
     */
    static {
        Configuration hbase_configuration = HBaseConfiguration.create();
        hbase_configuration.set("hbase.zookeeper.quorum", ConfigUntil.getConfig("zook.hosts"));
        hbase_configuration.set("hbase.zookeeper.property.clientPort", ConfigUntil.getConfig("zook.port"));

        try {
            hbase_connection = ConnectionFactory.createConnection(hbase_configuration);
        } catch (IOException e) {
            LOGGER.error("Create connection or admin error! " + e.getMessage(), e);
            try {
                hbase_connection.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 初始化命名空间：若命名空间存在则不创建
     *
     * @param namespace 命名空间
     */
    public void createNameSpace(String namespace) throws IOException {
        try {
            admin = hbase_connection.getAdmin();
            admin.getNamespaceDescriptor(namespace);
            LOGGER.error("NameSpace {} is exist!", namespace);
        } catch (NamespaceNotFoundException e) {
            admin.createNamespace(NamespaceDescriptor.create(namespace).build());

            LOGGER.info("Created namespace: {}", namespace);
            admin.close();
        }
    }

    /**
     * 删除表：先禁用再删除
     *
     * @param name 表名
     * @throws IOException io操作
     */
    public void deleteTable(String name) throws IOException {
        TableName tableName = TableName.valueOf(name);
        admin = hbase_connection.getAdmin();
        admin.disableTable(tableName);
        admin.deleteTable(tableName);
        LOGGER.info("Deleted table {} !", name);
        admin.close();
    }

    /**
     * 创建表：表存在时，先删除再创建；
     * 分区数为默认
     *
     * @param tableName    表名
     * @param columnFamily 列族
     * @throws IOException io操作
     */
    public void createTable(String tableName, String... columnFamily) throws IOException {
        createTable(tableName, 0, columnFamily);
    }


    /**
     * 创建表：表存在时，先删除再创建
     *
     * @param tableName    表名
     * @param regionCount  分区数
     * @param columnFamily 列族
     * @throws IOException io操作
     */
    public void createTable(String tableName, int regionCount, String... columnFamily) throws IOException {
        TableName name = TableName.valueOf(tableName);
        admin = hbase_connection.getAdmin();
        // 存在
        if (admin.tableExists(name)) {
            LOGGER.error("Table named {} already exist!", name);
            deleteTable(tableName);
        }

        createTableTemplate(name, regionCount, columnFamily);

    }

    /**
     * 表是否存在
     *
     * @param tableName 表名
     */
    public boolean tableExists(String tableName) throws IOException {
        TableName name = TableName.valueOf(tableName);
        return getAdmin().tableExists(name);
    }


    /**
     * 插入数据：单行、单列族 => 多列多值
     *
     * @param tableName    表名
     * @param rowKey       行
     * @param columnFamily 列族
     * @param columns      列
     * @param values       值(与列一一对应)
     */
    public void insertRecords(String tableName, String rowKey, String columnFamily, String[] columns, String[] values) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);
        Put put = new Put(Bytes.toBytes(rowKey));

        for (int i = 0; i < columns.length; i++) {
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columns[i]), Bytes.toBytes(values[i]));
            table.put(put);

        }
    }

    public void insertRecordss(String tableName, String rowKey,int commit_row, String columnFamily, String[] columns, ArrayList<String> values) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);

        String[] ui = new String[values.size()];
        List<Put> yu = new ArrayList<Put>();
        int count = 0;
        int count1 = 0;
        for (int i = 0; i < values.size(); i++) {
            Put put = new Put(Bytes.toBytes(rowKey + i));
            ui = values.get(i).split(",");
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columns[0]), Bytes.toBytes(ui[0]));
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columns[1]), Bytes.toBytes(ui[1]));
            yu.add(put);

            // values.remove(i);

            if (count == commit_row) {
                table.put(yu);
                count1+=1;
                 System.out.println("第"+count1+"次提交数据成功");
                count = 0;

            }
            count += 1;

        }
        table.put(yu);
        count1+=1;
        System.out.println("第"+count1+"次提交数据成功");
        table.close();
        hbase_connection.close();
    }

    /**
     * 插入数据：单行、单列族 => 单列单值
     *
     * @param tableName    表名
     * @param rowKey       行
     * @param columnFamily 列族
     * @param column       列名
     * @param value        列值
     */


    public void insertRecord(String tableName, String rowKey, String columnFamily, String column, String value) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(
                Bytes.toBytes(columnFamily),
                Bytes.toBytes(column),
                Bytes.toBytes(value));
        table.put(put);
    }


    /**
     * 删除一份数据
     *
     * @param tableName 表名
     * @param rowKey    行名
     */
    public void deleteRow(String tableName, String rowKey) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);
        Delete u_de = new Delete(Bytes.toBytes(rowKey));
        //u_de.addColumn(Bytes.toBytes("t1"),Bytes.toBytes("name"));
        table.delete(u_de);
        table.close();
        System.out.println("row  " + rowKey + " is deleted");
    }


    /**
     * 删除单行单列族记录
     *
     * @param tableName    表名
     * @param rowKey       行名
     * @param columnFamily 列族
     */
    public void deleteColumnFamily(String tableName, String rowKey, String columnFamily) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);
        table.delete(new Delete(rowKey.getBytes()).addFamily(Bytes.toBytes(columnFamily)));
    }

    /**
     * 删除单行单列族单列记录
     *
     * @param tableName    表名
     * @param rowKey       行名
     * @param columnFamily 列族
     * @param column       列
     */
    public void deleteColumn(String tableName, String rowKey, String columnFamily, String column) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);
        table.delete(new Delete(rowKey.getBytes()).addColumn(Bytes.toBytes(columnFamily),
                Bytes.toBytes(column)));
    }


    public void deleteList(String tableName, List<Delete> delete) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);
        table.delete(delete);
    }

    /**
     * 查找一行记录
     *
     * @param tableName 表名
     * @param rowKey    行名
     * @return 结果
     */
    public String selectRow(String tableName, String rowKey) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);

        Result result = table.get(new Get(rowKey.getBytes()));
        StringBuffer sb = new StringBuffer();
        resultToString(sb, result);

        return sb.toString();
    }

    /**
     * 查询单行、单列族、单列的值
     *
     * @param tableName    表名
     * @param rowKey       行名
     * @param columnFamily 列族
     * @param column       列名
     * @return 列值
     */
    public String selectValue(String tableName, String rowKey, String columnFamily, String column) throws IOException {
        Get get = new Get(rowKey.getBytes());
        get.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column));

        TableName name = TableName.valueOf(tableName);
        Result result = hbase_connection.getTable(name).get(get);
        return Bytes.toString(result.value());
    }

    /**
     * 全表扫描
     *
     * @param tableName 表名
     * @see Scan
     */
    public String scanAllRecord(String tableName) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);

        Scan scan = new Scan();
        StringBuffer sb = new StringBuffer();
        try (ResultScanner scanner = table.getScanner(scan)) {
            for (Result result : scanner) {
                resultToString(sb, result);
            }
        }

        return sb.toString();
    }

    /**
     * 拼接结果
     */
    private void resultToString(StringBuffer sb, Result result) {
        for (Cell cell : result.rawCells()) {
            sb.append(Bytes.toString(cell.getRowArray())).append("\t")
                    .append(Bytes.toString(cell.getFamilyArray())).append("\t")
                    .append(Bytes.toString(cell.getQualifierArray())).append("\t")
                    .append(Bytes.toString(cell.getValueArray())).append("\n");
        }
    }

    /**
     * 过滤器扫描：参考
     * https://www.cnblogs.com/Transkai/p/10727257.html<br>
     * 该 API 较老，使用时要注意查看过时方法上的注释，找新的 API 使用
     *
     * @param tableName 表名
     * @param filter    过滤器
     */
    public List<Cell> scanByFilter(String tableName, Filter filter) throws IOException {
        List<Cell> resultList = new ArrayList<>();
        TableName name = TableName.valueOf(tableName);
        Table table = hbase_connection.getTable(name);

        Scan scan = new Scan();
        scan.setFilter(filter);
        try (ResultScanner scanner = table.getScanner(scan)) {
            for (Result result : scanner) {
                resultList.addAll(Arrays.asList(result.rawCells()));
            }
        }

        return resultList;
    }


    /**
     * 创建表
     *
     * @param tableName    表名
     * @param regionCount  分区数
     * @param columnFamily 列族
     */
    private void createTableTemplate(TableName tableName, int regionCount, String... columnFamily) throws IOException {
        try {
            admin = hbase_connection.getAdmin();
            TableDescriptorBuilder tableBuilder = TableDescriptorBuilder.newBuilder(tableName);
            // 增加列族
            tableBuilder.setColumnFamilies(createColumnFamilyList(columnFamily));

            // 无分区（未指定）
            if (regionCount <= 0) {
                admin.createTable(tableBuilder.build());
                System.out.println("创建 "+tableName+" hbase表成功！");
            } else {
                // 预分区
                byte[][] splitKey = getSplitKeys(regionCount);
                admin.createTable(tableBuilder.build(), splitKey);
            }
            LOGGER.info("Created table named {}", tableName);
        } catch (IOException e) {
            LOGGER.error("Create table error, " + e.getMessage(), e);
            admin.close();
            hbase_connection.close();
        }
    }

    /**
     * 列族描述器：没有内容时，自定义加一个列族 info
     *
     * @param columnFamily 列族名
     * @return 列族描述器
     */
    private List<ColumnFamilyDescriptor> createColumnFamilyList(String... columnFamily) {
        List<ColumnFamilyDescriptor> results = new ArrayList<>();
        // 设置默认列族 info
        if (columnFamily == null || columnFamily.length == 0) {
            columnFamily = new String[]{"info"};
        }

        for (String family : columnFamily) {
            ColumnFamilyDescriptorBuilder descriptorBuilder =
                    ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(family));

            results.add(descriptorBuilder.setBlockCacheEnabled(true).build());
        }

        return results;
    }

    /**
     * 生成分区键
     *
     * @param regionCount 分区数
     * @return 多个分区键
     */
    private byte[][] getSplitKeys(int regionCount) {
        int splitKeyCount = regionCount - 1;
        byte[][] bytes = new byte[splitKeyCount][];

        List<byte[]> byteList = new ArrayList<>();
        for (int i = 0; i < splitKeyCount; i++) {
            String key = i + "|";
            byteList.add(Bytes.toBytes(key));
        }

        byteList.toArray(bytes);
        return bytes;
    }


    public Connection getConnection() {
        return hbase_connection;
    }

    public Admin getAdmin() {
        try {
            admin = hbase_connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return admin;
    }


    /**
     * 用于创建线程池。
     * ThreadFactory实现类：重命名线程
     *
     * @see ThreadFactory
     */


    public static void close() throws IOException {
        //关闭连接
        if (admin != null) {
            admin.close();
        }
        if (hbase_connection != null) {
            hbase_connection.close();
        }
    }
    /*
    private static class ThreadFactoryImpl implements ThreadFactory {
        private final String name;
        private AtomicInteger id = new AtomicInteger(1);

        private ThreadFactoryImpl(String name){
            this.name = "ThreadFactory-" + name + "-" + id.getAndIncrement();
        }

        @Override
        public Thread newThread(@Nonnull Runnable runnable) {
            return new Thread(runnable, name);
        }
    }*/
}


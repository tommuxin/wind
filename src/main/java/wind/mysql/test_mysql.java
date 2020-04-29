package wind.mysql;

import java.sql.*;
import java.util.*;

public class test_mysql {

    public final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public final String DB_URL = "jdbc:mysql://10.190.192.111:3306/druid?useUnicode=true&characterEncoding=UTF-8";
    public final String USER = "driui";
    public final String PASS = "Swarp_123";
    public ResultSet v_select(String sert_sqle) throws ClassNotFoundException, SQLException {


        Connection conn1 = null;
        Statement stmt1 = null;

        // 注册 JDBC 驱动
        Class.forName(JDBC_DRIVER);

        // 打开链接
        System.out.println("连接数据库...");
        conn1 = DriverManager.getConnection(DB_URL, USER, PASS);

        System.out.println(" 实例化Statement对象...");


        PreparedStatement psts = conn1.prepareStatement(sert_sqle);
        //删除日期

        ResultSet mysql_se = psts.executeQuery();

        return mysql_se;


    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        test_mysql yy =new test_mysql();
        yy.v_select("create table test1(name int)");
    }
}

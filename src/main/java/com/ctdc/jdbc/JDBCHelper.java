package com.ctdc.jdbc;

import com.ctdc.conf.ConfigurationManager;
import com.ctdc.constant.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;

/**
 * Created by CTWLPC on 2018/1/18.
 */
public class JDBCHelper {

    static {
        try {
            // 第一步：加载驱动类
            String driver = ConfigurationManager.getProperty(Constants.JDBC_DRIVER);
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 第二步，实现JDBCHelper的单例化
    // 为什么要实现代理化呢？因为它的内部要封装一个简单的内部的数据库连接池
    // 为了保证数据库连接池有且仅有一份，所以就通过单例的方式
    // 保证JDBCHelper只有一个实例，实例中只有一份数据库连接池
    private static JDBCHelper instance = null;

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static JDBCHelper getInstance() {
        if (instance == null) {
            synchronized (JDBCHelper.class) {
                if (instance == null) {
                    instance = new JDBCHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 数据库连接池
     */
    private LinkedList<Connection> datasource = new LinkedList<Connection>();

    /**
     * 第三步：实现单例的过程中，创建唯一的数据库连接池
     * 私有化构造方法
     * JDBCHelper在整个程序运行声明周期中，只会创建一次实例
     * 在这一次创建实例的过程中，就会调用JDBCHelper()构造方法
     * 此时，就可以在构造方法中，去创建自己唯一的一个数据库连接池
     */
    private JDBCHelper() {
        // 首先第一步，获取数据库连接池的大小，就是说，数据库连接池中要放多少个数据库连接
        // 这个，可以通过在配置文件中配置的方式，来灵活的设定
        int datasourceSize = ConfigurationManager.getInteger(
                Constants.JDBC_DATASOURCE_SIZE);

        // 然后创建指定数量的数据库连接，并放入数据库连接池中
        for (int i = 0; i < datasourceSize; i++) {
            boolean local = ConfigurationManager.getBoolean(Constants.SPARK_LOCAL);
            String url = null;
            String user = null;
            String password = null;
            if (local) {
                url = ConfigurationManager.getProperty(Constants.JDBC_URL);
                user = ConfigurationManager.getProperty(Constants.JDBC_PASSWORD);
                password = ConfigurationManager.getProperty(Constants.JDBC_PASSWORD);
            } else {
                url = ConfigurationManager.getProperty(Constants.JDBC_URL_PROD);
                user = ConfigurationManager.getProperty(Constants.JDBC_USER_PROD);
                password = ConfigurationManager.getProperty(Constants.JDBC_PASSWORD_PROD);
            }

            // 获取数据库连接
            try {
                Connection conn = DriverManager.getConnection(url, user, password);
                datasource.push(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 第四步，提供获取数据库连接的方法
     * 有可能，你去获取的时候，这个时候，连接都被用光了，你暂时获取不到数据库连接
     * 所以我们要自己编码实现一个简单的等待机制，去等待获取到数据库连接
     */
    public synchronized Connection getConnection() {
        while (datasource.size() == 0) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return datasource.poll();
    }

//    public synchronized Connection getConnection() {
//        while(datasource.size() == 0) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return datasource.poll();
//    }



    /**
     * 第五步：开发增删改查的方法
     * 1、执行增删改SQL语句的方法
     * 2、执行查询SQL语句的方法
     * 3、批量执行SQL语句的方法
     */

}


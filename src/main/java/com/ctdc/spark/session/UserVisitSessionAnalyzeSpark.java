package com.ctdc.spark.session;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by CTWLPC on 2018/1/18.
 */
public class UserVisitSessionAnalyzeSpark {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("UserVisitSessionAnalyzeSpark")
                .setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);



    }


}

package com.ctdc;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("App").setMaster("local");
        SparkContext sc = new SparkContext(conf);

        System.out.println("Hello World!");
    }
}

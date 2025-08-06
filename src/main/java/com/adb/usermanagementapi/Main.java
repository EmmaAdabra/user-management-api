package com.adb.usermanagementapi;

import org.apache.catalina.startup.Tomcat;

public class Main {
    public static void main(String[] args) throws Exception{
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.addWebapp("", System.getProperty("user.dir") + "/src/main/webapp");
        tomcat.getConnector();
        tomcat.start();
        tomcat.getServer().await();
    }
}
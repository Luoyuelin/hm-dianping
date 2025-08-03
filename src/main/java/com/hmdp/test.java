package com.hmdp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @Author：lyl
 * @Package：com.hmdp
 * @Project：hm-dianping
 * @Date：2025/6/13
 * @Description：连接 Oracle 并列出当前用户的所有表
 */
public class test {

    public static void main(String[] args) {
        // JDBC URL，使用 SID 格式连接 Oracle
        String url = "jdbc:oracle:thin:@10.131.18.160:1524:helowin";
        String username = "oracle_user";     // 你的用户名
        String password = "200008120";       // 你的密码
        System.setProperty("oracle.jdbc.convertNcharLiterals", "true");


        try {
            // 加载 Oracle JDBC 驱动
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // 创建数据库连接
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ 连接成功！");

            // 创建 Statement 对象
            Statement stmt = conn.createStatement();

            // 打印当前登录用户
            ResultSet userRs = stmt.executeQuery("SELECT USER FROM dual");
            if (userRs.next()) {
                System.out.println("当前登录用户是：" + userRs.getString(1));
            }
            userRs.close();

            // 查询当前用户的所有表
            String sql = "SELECT table_name FROM user_tables";
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("📋 当前用户下的表有：");
            while (rs.next()) {
                System.out.println("- " + rs.getString("table_name"));
            }

            // 关闭资源
            rs.close();
            stmt.close();
            conn.close();
            System.out.println("✅ 查询结束，连接关闭。");

        } catch (Exception e) {
            System.err.println("❌ 出现异常：");
            e.printStackTrace();
        }
    }
}

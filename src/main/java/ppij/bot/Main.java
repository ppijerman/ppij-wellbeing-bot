package ppij.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.math.BigInteger;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

public class Main {
    static final String DB_URL = "jdbc:postgresql://" +System.getenv("DB_HOST") + ":" + System.getenv("DB_PORT") + "/" + System.getenv("DB_NAME");
    static final String USER = System.getenv("DB_USER");
    static final String PASS = System.getenv("DB_PASSWORD");

    public static void main(String[] args) throws LoginException, ClassNotFoundException {
        Connection conn = sql_queries();

        if (conn ==null) return;


        System.out.println("Start Discord Bot");
        JDABuilder builder = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN")); //DISCORD_TOKEN
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.enableIntents(GatewayIntent.DIRECT_MESSAGES);
        builder.addEventListeners(new BotListener(conn));
        JDA jda = builder.build();


    }


    public static Connection sql_queries() throws ClassNotFoundException {
        // Open a connection
        Class.forName("org.postgresql.Driver");

        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); //DB_HOST, DB_USER, DB_PASSWORD
            Statement stmt = conn.createStatement();
        ) {
            System.out.println("connected to database successfully...");
            String sql = "DROP TABLE USER_REGISTRATION";
            stmt.executeUpdate(sql);
            sql = "CREATE TABLE USER_REGISTRATION " +
                    "(id VARCHAR(255) not NULL, " +
                    " NAME VARCHAR(255), " +
                    " HOBBY VARCHAR(255), " +
                    " REASON VARCHAR(255), " +
                    " PRIMARY KEY ( id ))";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO USER_REGISTRATION VALUES (102, 'Deni', 'Makan', 'Bosan')";
            stmt.executeUpdate(sql);

            String query = "select * from USER_REGISTRATION";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String ID = rs.getString("id");

                System.out.println(ID + ", " + rs.getString("NAME") + ", " + rs.getString("HOBBY") +
                        ", " + rs.getString("REASON"));
            }

            System.out.println("SQL Queries Ended");

            return conn;


//            GET TABLE NAMES
//            DatabaseMetaData metaData = conn.getMetaData();
//            String[] types = {"TABLE"};
//            //Retrieving the columns in the database
//            ResultSet tables = metaData.getTables(null, null, "%", types);
//            while (tables.next()) {
//                System.out.println("Table:" + tables.getString("TABLE_NAME"));
//            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

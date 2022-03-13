package ppij.bot;

import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.math.BigInteger;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

public class Main {

    private static HikariDataSource ds;

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
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.addEventListeners(new BotListener(conn));
        JDA jda = builder.build();

    }


    public static Connection sql_queries() throws ClassNotFoundException {
        // Open a connection
        Class.forName("org.postgresql.Driver");

        try{
            ds = new HikariDataSource();
            ds.setJdbcUrl(DB_URL);
            ds.setUsername(USER);
            ds.setPassword(PASS);
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();

            System.out.println("connected to database successfully...");
            String sql = "DROP TABLE USER_REGISTRATION";
            stmt.executeUpdate(sql);


            sql = "CREATE TABLE USER_REGISTRATION " +
                    "(id VARCHAR(255) not NULL, " +
                    " NAMA VARCHAR(255), " +
                    " UMUR int , " +
                    " KESIBUKAN VARCHAR(255), " +
                    " DOMISILI VARCHAR(255), " +
                    " INSTITUSI VARCHAR(255), " +
                    " MATA_KULIAH VARCHAR(255), " +
                    " EMAIL VARCHAR(255), " +
                    " ALASAN_HARAPAN VARCHAR(255), " +
                    " PRIMARY KEY ( id ))";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO USER_REGISTRATION VALUES (102123, 'Deni', '18', 'Sok Sibuk', " +
                    "'Munich', 'TUM', 'Informatik', 'iniemail@gmail.com', 'gabut')";

            stmt.executeUpdate(sql);

            printAllEntries(conn);

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

    public static void printAllEntries(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "select * from USER_REGISTRATION";
        ResultSet rs = stmt.executeQuery(query);

        System.out.println("ID \t||\t NAMA \t||\t UMUR \t||\t KESIBUKAN \t||\t DOMISILI \t||\t INSTITUSI " +
                "\t||\t MATA_KULIAH \t||\t EMAIL \t||\t ALASAN_HARAPAN");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------");


        while (rs.next()) {
            String ID = rs.getString("id");

            System.out.printf("%s \t||\t %S \t||\t %S \t||\t %S \t||\t %S \t||\t %S \t||\t %S \t||\t %S \r\n" +
                            "\t||\t %S ", rs.getString("id"), rs.getString("NAMA"),
                    rs.getInt("UMUR"), rs.getString("KESIBUKAN"),
                    rs.getString("DOMISILI"), rs.getString("INSTITUSI"),
                    rs.getString("MATA_KULIAH"), rs.getString("EMAIL"), rs.getString("ALASAN_HARAPAN"));
        }

    }
}

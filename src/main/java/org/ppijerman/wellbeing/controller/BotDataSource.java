package org.ppijerman.wellbeing.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BotDataSource {
    static final String DB_URL = "jdbc:postgresql://" + System.getenv("DB_HOST") + ":" + System.getenv("DB_PORT") + "/" + System.getenv("DB_NAME");
    static final String USER = System.getenv("DB_USER");
    static final String PASS = System.getenv("DB_PASSWORD");

    private final HikariDataSource ds;
    private final Logger log = LoggerFactory.getLogger(BotDataSource.class);

    public BotDataSource() throws SQLException {
        log.info("Creating datasource");
        this.ds = new HikariDataSource();
        this.ds.setJdbcUrl(DB_URL);
        this.ds.setUsername(USER);
        this.ds.setPassword(PASS);
        this.ds.setDriverClassName("org.postgresql.Driver");
        this.ds.setLoginTimeout(10);
        log.info("Done create datasource, initializing database startup");
        this.initDbStartup();
    }

    private void initDbStartup() throws SQLException {
        final String createTableQuery = "CREATE TABLE IF NOT EXISTS USER_REGISTRATION " +
                "(id VARCHAR(255) NOT NULL, " +
                " NAMA VARCHAR(255), " +
                " UMUR int , " +
                " KESIBUKAN VARCHAR(255), " +
                " DOMISILI VARCHAR(255), " +
                " INSTITUSI VARCHAR(255), " +
                " MATA_KULIAH VARCHAR(255), " +
                " EMAIL VARCHAR(255), " +
                " ALASAN_HARAPAN VARCHAR(255), " +
                " PRIMARY KEY ( id ))";
        final String queryAllRegistration = "SELECT * FROM USER_REGISTRATION";

        try (Connection conn = ds.getConnection();
             PreparedStatement createTablePs = conn.prepareStatement(createTableQuery);
             PreparedStatement queryAllRegPs = conn.prepareStatement(queryAllRegistration)
        ) {
            log.info("Connected to database successfully.");
            createTablePs.executeUpdate();
            log.info("Table created or already exists, printing on debug channel all values stored.");
            ResultSet rs = queryAllRegPs.executeQuery();
            while (rs.next()) {
                log.debug("{} \t||\t {} \t||\t {} \t||\t {} \t||\t {} \t||\t {} \t||\t {} \t||\t {} \r\n" +
                                "\t||\t {} ", rs.getString("id"), rs.getString("NAMA"),
                        rs.getInt("UMUR"), rs.getString("KESIBUKAN"),
                        rs.getString("DOMISILI"), rs.getString("INSTITUSI"),
                        rs.getString("MATA_KULIAH"), rs.getString("EMAIL"), rs.getString("ALASAN_HARAPAN"));
            }
            log.info("SQL Queries Ended");
        } catch (SQLException e) {
            log.error("Error on initializing database with error: {}", e.getMessage());
        }
    }

    public DataSource getDataSource() {
        return this.ds;
    }
}

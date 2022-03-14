package ppij.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ppij.bot.controller.BotDataSource;
import ppij.bot.controller.BotListener;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;

public class Main {
    private final Logger log = LoggerFactory.getLogger(Main.class);
    private final BotDataSource ds;
    private final JDA jda;

    public static void main(String[] args) throws LoginException, SQLException {
        new Main();
    }

    private Main() throws LoginException, SQLException {
        log.info("Starting Discord Bot");
        this.ds = new BotDataSource();

        JDABuilder builder = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN")); //DISCORD_TOKEN
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.enableIntents(GatewayIntent.DIRECT_MESSAGES);
        builder.addEventListeners(new BotListener(this.ds.getDataSource()));
        this.jda = builder.build();
        log.info("Discord Bot started");
    }
}

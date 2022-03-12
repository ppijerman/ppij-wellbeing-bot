package ppij.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.*;

import java.awt.*;
import java.sql.*;
import java.util.HashMap;

public class BotListener extends ListenerAdapter{

    private static String main_channel = "818603289662062595";

    private Connection conn;


//    User A -> [nama, umur, kesibukan, domisili, institusi, Mata Kuliah, E-Mail, Mengapa tertarik dengan discord
//    , apa yg diharapkan, index]


//      HOW TO DO SAFE QUERY
//    final String queryCheck = "SELECT * from USER_REGISTRATION WHERE id = ?";
//    final PreparedStatement ps;
//    ps = conn.prepareStatement(queryCheck);
//            ps.setString(1, user.getId());
//    final ResultSet resultSet = ps.executeQuery();
//            if (resultSet.next() && resultSet.getString("NAME") != null
//                    && resultSet.getString("HOBBY") != null &&
//                    resultSet.getString("REASON") != null) {
//        return true;
//    }



    private HashMap<String,Integer> map = null;

    public BotListener(Connection conn) {
        this.conn = conn;
        this.map = new HashMap<>();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        MessageChannel channel = event.getChannel();
        channel.sendMessage("bruh").queue();

        if ("!fillForm".equals(event.getMessage().toString())){
//            start
            fillFormViaText(event.getAuthor());
        }


        event.getAuthor().openPrivateChannel().queue((privateChannel)->
        {
            privateChannel.sendMessage("content").queue();
        });

    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getUser().isBot()) return;

        event.getGuild().getDefaultChannel().sendMessage("Welcome").queue();

        if (validateUser(event.getUser()))
            return;

        fillFormViaText(event.getUser());

    }

    public boolean validateUser(User user){
        try {
            final String query = "SELECT * from USER_REGISTRATION WHERE id = " + user.getId();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next() && rs.getString("NAME") != null
                    && rs.getString("HOBBY") != null &&
                    rs.getString("REASON") != null) {
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public void fillFormViaText(User user){
        map.put(user.getId(),0);

    }


}

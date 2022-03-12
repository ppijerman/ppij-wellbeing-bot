package ppij.bot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.entities.PrivateChannelImpl;

import java.sql.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Enum.valueOf;
import static java.util.concurrent.TimeUnit.SECONDS;

public class BotListener extends ListenerAdapter{

    private static String main_channel = "818603289662062595";

    private Connection conn;

    static final Integer NAMA = 1;
    static final Integer UMUR = 2;
    static final Integer KESIBUKAN = 3;
    static final Integer DOMISILI = 4;
    static final Integer INSTITUSI = 5;
    static final Integer MATA_KULIAH = 6;
    static final Integer EMAIL = 7;
    static final Integer ALASAN_HARAPAN = 8;

    static final String NAMA_Message = "";
    static final String UMUR_Message = "";
    static final String KESIBUKAN_Message = "";
    static final String DOMISILI_Message = "";
    static final String INSTITUSI_Message = "";
    static final String EMAIL_Message = "";
    static final String ALASAN_HARAPAN_Message = "";
    static final String THANKYOU_Message = "";


    private HashMap<String, Object[]> map;

    public BotListener(Connection conn) {
        this.conn = conn;
        this.map = new HashMap<String, Object[]>();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        System.out.println(event.getMember());
        Guild guild1 = event.getJDA().getGuildById("818603289662062592");
        System.out.println(guild1.getMemberById(event.getAuthor().getId()));
        System.out.println(guild1.getMember(event.getAuthor()));


//         start again
        if ("!fill".equals(event.getMessage().getContentStripped())){
            System.out.println("fill");
            fillFormViaText(event.getAuthor());
            return;
        }

        if (map.get(event.getAuthor().getId()) == null) return;

        if (event.getChannel().getClass().equals(PrivateChannelImpl.class)){
            event.getAuthor().openPrivateChannel().queue((privateChannel)->
            {
                Object[] val = map.get(event.getAuthor().getId());
                Integer step = (Integer) val[0];
                System.out.println(step);
                String message = event.getMessage().getContentStripped();

                if (step == NAMA){
                    val[step] = message;
                    val[0] = UMUR;
                    privateChannel.sendMessage("(2/9) How old are you?").queue();

                } else if (step == UMUR){
                    try {
                        int age = Integer.parseInt(message);
                        if (age>=0 && age <=99) {
                            val[step] = age;
                            val[0] = KESIBUKAN;
                            privateChannel.sendMessage("(3/9) Kesibukan?").queue();
                            privateChannel.sendMessage("Ketik Angka berikut bila sesuai\n" +
                                    "1) Bachelorstudium\n" +
                                    "2) Masterstudium\n" +
                                    "3) Ausbildung\n" +
                                    "Ketik kesibukan anda bila tidak sesuai").queue();
                        } else {
                            privateChannel.sendMessage("Invalid Age Format (0-99)").queue();
                            privateChannel.sendMessage("(2/9) How old are you?").queue();
                        }
                    }
                    catch (NumberFormatException e) {
                        privateChannel.sendMessage("Invalid Age Format (0-99)").queue();
                        privateChannel.sendMessage("(2/9) How old are you?").queue();
                    }
                } else if (step == KESIBUKAN){
                    val[0] = DOMISILI;

                    try {
                        switch (Integer.parseInt(message)){
                            case 1:
                                System.out.println("sibuk ya");
                                val[KESIBUKAN] = "Kungfu Master";
                                break;
                            case 2:
                                System.out.println("sibuk apa");
                                val[KESIBUKAN] = "Karate Master";
                                break;
                            default:
                                val[KESIBUKAN] = message;
                        }
                    }
                    catch (NumberFormatException e) {
                        val[KESIBUKAN] = message;
                    }

                    privateChannel.sendMessage("(4/9) Domisili?").queue();
                    privateChannel.sendMessage("1)Bayern, 2) BAWU").queue();

                } else if (step == DOMISILI){

                    boolean failed = false;

                    try {
                        switch (Integer.parseInt(message)){
                            case 1:
                                val[DOMISILI] = "Bayern";
                                break;
                            case 2:
                                val[DOMISILI] = "Baden-Wurttemberg";
                                break;
                            default:
                                privateChannel.sendMessage("Invalid Input").queue();
                                privateChannel.sendMessage("(4/9) Domisili?").queue();
                                privateChannel.sendMessage("1)Bayern, 2) BAWU").queue();
                                failed = true;
                        }
                    }
                    catch (NumberFormatException e) {
                        privateChannel.sendMessage("Invalid Input").queue();
                        privateChannel.sendMessage("(4/9) Domisili?").queue();
                        privateChannel.sendMessage("1)Bayern, 2) BAWU").queue();
                        failed = true;
                    }

                    if (!failed){
                        val[0] = INSTITUSI;
                        privateChannel.sendMessage("(5/9) Institusi?").queue();
                    }

                } else if (step == INSTITUSI){
                    val[0] = MATA_KULIAH;
                    val[step] = message;
                    privateChannel.sendMessage("(6/9) Mata Kuliah?").queue();

                } else if (step == MATA_KULIAH){
                    val[0] = EMAIL;
                    val[step] = message;
                    privateChannel.sendMessage("(7/9) What is your Email Address?").queue();

                } else if (step == EMAIL){

                    String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

                    Pattern pattern = Pattern.compile(regex);

                    Matcher matcher = pattern.matcher(message);
                    if (matcher.matches()){
                        val[0] = ALASAN_HARAPAN;
                        val[step] = message;
                        privateChannel.sendMessage("Terimakasih sudah mengisi formulir kami, " +
                                "berikan alasan bergabung dengan server ini dan apa yang anda harapkan dari " +
                                "bergabung dengan server ini untuk mengakses server atau ketik submit").queue();
                    } else {
                        privateChannel.sendMessage("Invalid Email Address").queue();
                        privateChannel.sendMessage("(7/9) What is your Email Address?").queue();
                    }

                } else if (step == ALASAN_HARAPAN){
                    if (!message.contains("submit")){
                        val[ALASAN_HARAPAN] = message;
                    }

                    try {

//                        TODO if user wants to change
//                        TODO uncached user that has no activity in server for a while

                        //      HOW TO DO SAFE QUERY
                        final String queryCheck = "INSERT INTO USER_REGISTRATION VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        final PreparedStatement ps = conn.prepareStatement(queryCheck);
                        ps.setString(1, event.getAuthor().getId());
                        ps.setString(2, (String) val[1]);
                        ps.setInt(3, (Integer) val[2]);
                        ps.setString(4, (String) val[3]);
                        ps.setString(5, (String) val[4]);
                        ps.setString(6, (String) val[5]);
                        ps.setString(7, (String) val[6]);
                        ps.setString(8, (String) val[7]);
                        ps.setString(9, (String) val[8]);

                        int row = ps.executeUpdate();
                        System.out.println(row); //1

                        Main.printAllEntries(conn);


                        Guild guild = event.getJDA().getGuildById("818603289662062592");
                        Role role = guild.getRoleById("818634084590813196");


                        System.out.println(guild.getName());
                        System.out.println(event.getAuthor().getId());
                        System.out.println(guild.getMemberById(event.getAuthor().getId()));
                        System.out.println(guild.getMember(event.getAuthor()));


                        if (event.getAuthor() == null){
                            System.out.println("this is null");
                        }
                        guild.addRoleToMember(guild.getMember(event.getAuthor()), role).queue();
                        privateChannel.sendMessage("Enjoy your time in the server and please be active or whatever").queue();

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }


                } else {
                    System.err.println("ERROR, weird Last Question Asked");
                }
            });
        }

    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getUser().isBot()) return;

        event.getGuild().getDefaultChannel().sendMessage("Welcome").queue();

        if (validateUser(event.getUser())) {
            Guild guild = event.getGuild();
            Role role = guild.getRoleById("818634084590813196");
            guild.addRoleToMember(event.getMember(), role).queue();
            return;
        }

        fillFormViaText(event.getUser());

    }

    public boolean validateUser(User user){
        try {
            final String query = "SELECT * from USER_REGISTRATION WHERE id = " + user.getId();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public void fillFormViaText(User user){
        Object[] val = new Object[9];
        val[0] = NAMA;

        map.put(user.getId(), val);
        user.openPrivateChannel().queue((privateChannel)->
        {
            privateChannel.sendMessage("Welcome to the server, to validate your account, " +
                    "please answer the following questions. \n (1/9) What is your Name?").queue();
        });


//        Remove key value after 10 minutes
        final ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);

        final Runnable beeper = new Runnable() {
            public void run() {
                System.out.printf("Remove User: %s from HashMap %n", user.getAsTag());
            }
        };
        final ScheduledFuture<?> beeperHandle = scheduler.schedule(beeper, 600, SECONDS);


    }


}

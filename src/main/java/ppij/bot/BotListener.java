package ppij.bot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.entities.PrivateChannelImpl;

import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    static final String WELCOME_Message = "Hi %s! Welcome to Wellbeing by PPI Jerman! Before you can start interacting with other members, let’s fill out this little questionnaire!\n";
    static final String NAMA_Message = "(1/7) Full Name:";
    static final String UMUR_Message = "(2/7) Age:\n" +
            "1) 18-24\n" +
            "2) 25-31\n" +
            "3) 32-38\n" +
            "4) 39-45\n" +
            "5) 46-55\n" +
            "6) 56 and over";
    static final String KESIBUKAN_Message = "(3/7) Occupation:\n" +
            "1) Studienkolleg\n" +
            "2) Studium (Bachelor)\n" +
            "3) Studium (Master)\n" +
            "4) Ausbildung\n" +
            "5) Sprachkurs\n" +
            "Others";
    static final String DOMISILI_Message = "(4/7) Where are you from?\n" +
            "1) Baden-Württemberg\n" +
            "2) Bayern\n" +
            "3) Berlin\n" +
            "4) Brandenburg\n" +
            "5) Bremen\n" +
            "6) Hamburg\n" +
            "7) Hessen\n" +
            "8) Mecklenburg-Vorpommern\n" +
            "9) Niedersachsen\n" +
            "10) Nordrhein-Westfalen\n" +
            "11) Rheinland-Pfalz\n" +
            "12) Saarland\n" +
            "13) Sachsen\n" +
            "14) Sachsen-Anhalt\n" +
            "15) Schleswig-Holstein\n" +
            "16) Thüringen";
    static final String INSTITUSI_Message = "(5/7) Institution Name:";
    static final String EMAIL_Message = "(7/7) E-Mail:";
    static final String MATA_KULIAH_Message = "(6/7) Study Field:";
    static final String ALASAN_HARAPAN_Message = "Why are you interested in joining our community and what are your expectations?";
    static final String THANKYOU_Message = "Great! Just one more thing to do. Please go back to our discord server and read the rules on our ‘rules’ channel and you’re all set!";


    private HashMap<String, Object[]> map;

    public BotListener(Connection conn) {
        this.conn = conn;
        this.map = new HashMap<String, Object[]>();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

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
                String message = event.getMessage().getContentStripped();

                if (step == NAMA){
                    val[step] = message;
                    val[0] = UMUR;
                    privateChannel.sendMessage(UMUR_Message).queue();

                } else if (step == UMUR){
                    try {
                        int age = Integer.parseInt(message);
                        if (age>=0 && age <=99) {
                            val[step] = age;
                            val[0] = KESIBUKAN;
                            privateChannel.sendMessage(KESIBUKAN_Message).queue();
                        } else {
                            privateChannel.sendMessage("Invalid Age Format (0-99)").queue();
                            privateChannel.sendMessage(UMUR_Message).queue();
                        }
                    }
                    catch (NumberFormatException e) {
                        privateChannel.sendMessage("Invalid Age Format (0-99)").queue();
                        privateChannel.sendMessage(UMUR_Message).queue();
                    }
                } else if (step == KESIBUKAN){
                    val[0] = DOMISILI;

                    try {
                        switch (Integer.parseInt(message)){
                            case 1:
                                val[KESIBUKAN] = "Studienkolleg";
                                break;
                            case 2:
                                val[KESIBUKAN] = "Studium (Bachelor)";
                                break;
                            case 3:
                                val[KESIBUKAN] = "Studium (Master)";
                                break;
                            case 4:
                                val[KESIBUKAN] = "Ausbildung";
                                break;
                            case 5:
                                val[KESIBUKAN] = "Sprachkurs";
                                break;
                            default:
                                val[KESIBUKAN] = message;
                        }
                    }
                    catch (NumberFormatException e) {
                        val[KESIBUKAN] = message;
                    }

                    privateChannel.sendMessage(DOMISILI_Message).queue();
                } else if (step == DOMISILI){
                    boolean failed = false;

                    try {
                        switch (Integer.parseInt(message)){
                            case 1:
                                val[DOMISILI] = "Baden-Wurttemberg";
                                break;
                            case 2:
                                val[DOMISILI] = "Bayern";
                                break;
                            case 3:
                                val[DOMISILI] = "Berlin";
                                break;
                            case 4:
                                val[DOMISILI] = "Brandenburg";
                                break;
                            case 5:
                                val[DOMISILI] = "Bremen";
                                break;
                            case 6:
                                val[DOMISILI] = "Hamburg";
                                break;
                            case 7:
                                val[DOMISILI] = "Hessen";
                                break;
                            case 8:
                                val[DOMISILI] = "Mecklenburg-Vorpommern";
                                break;
                            case 9:
                                val[DOMISILI] = "Niedersachsen";
                                break;
                            case 10:
                                val[DOMISILI] = "Nordrhein-Westfalen";
                                break;
                            case 11:
                                val[DOMISILI] = "Rheinland-Pfalz";
                                break;
                            case 12:
                                val[DOMISILI] = "Saarland";
                                break;
                            case 13:
                                val[DOMISILI] = "Sachsen";
                                break;
                            case 14:
                                val[DOMISILI] = "Sachsen-Anhalt";
                                break;
                            case 15:
                                val[DOMISILI] = "Schleswig-Holstein";
                                break;
                            case 16:
                                val[DOMISILI] = "Thüringen";
                                break;
                            default:
                                privateChannel.sendMessage("Invalid Input\n" + DOMISILI_Message).queue();
                                failed = true;
                        }
                    }
                    catch (NumberFormatException e) {
                        privateChannel.sendMessage("Invalid Input\n" + DOMISILI_Message).queue();
                        failed = true;
                    }

                    if (!failed){
                        val[0] = INSTITUSI;
                        privateChannel.sendMessage(INSTITUSI_Message).queue();
                    }

                } else if (step == INSTITUSI){
                    val[0] = MATA_KULIAH;
                    val[step] = message;
                    privateChannel.sendMessage(MATA_KULIAH_Message).queue();
                } else if (step == MATA_KULIAH){
                    val[0] = EMAIL;
                    val[step] = message;
                    privateChannel.sendMessage(EMAIL_Message).queue();
                } else if (step == EMAIL){
                    String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(message);
                    if (matcher.matches()){
                        val[0] = ALASAN_HARAPAN;
                        val[step] = message;
                        privateChannel.sendMessage(ALASAN_HARAPAN_Message).queue();
                    } else {
                        privateChannel.sendMessage("Invalid Email Address\n" + EMAIL_Message).queue();
                    }

                } else if (step == ALASAN_HARAPAN){
                    if (!message.contains("submit")){
                        val[ALASAN_HARAPAN] = message;
                    }

                    try {
                        //      HOW TO DO SAFE QUERY
                        String queryCheck;
                        PreparedStatement ps;
                        if (validateUser(event.getAuthor())){

                                queryCheck = "UPDATE USER_REGISTRATION SET NAMA=? ," +
                                    "UMUR=?, KESIBUKAN=?, DOMISILI=?, INSTITUSI=?, MATA_KULIAH=?," +
                                    "EMAIL=?, ALASAN_HARAPAN=? WHERE id=?";
                            ps = conn.prepareStatement(queryCheck);
                            ps.setString(1, (String) val[1]);
                            ps.setInt(2, (Integer) val[2]);
                            ps.setString(3, (String) val[3]);
                            ps.setString(4, (String) val[4]);
                            ps.setString(5, (String) val[5]);
                            ps.setString(6, (String) val[6]);
                            ps.setString(7, (String) val[7]);
                            ps.setString(8, (String) val[8]);
                            ps.setString(9, event.getAuthor().getId());
                        } else {
                            queryCheck = "INSERT INTO USER_REGISTRATION VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            ps = conn.prepareStatement(queryCheck);
                            ps.setString(1, event.getAuthor().getId());
                            ps.setString(2, (String) val[1]);
                            ps.setInt(3, (Integer) val[2]);
                            ps.setString(4, (String) val[3]);
                            ps.setString(5, (String) val[4]);
                            ps.setString(6, (String) val[5]);
                            ps.setString(7, (String) val[6]);
                            ps.setString(8, (String) val[7]);
                            ps.setString(9, (String) val[8]);

                        }


                        ps.executeUpdate();
                        Main.printAllEntries(conn);
                        Guild guild = event.getJDA().getGuildById("818603289662062592");
                        Role role = guild.getRoleById("818634084590813196");

                        guild.addRoleToMember(event.getAuthor().getId(), role).queue();
                        privateChannel.sendMessage(THANKYOU_Message).queue();

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

        event.getGuild().getDefaultChannel().sendMessage("Welcome <@" + event.getUser().getId() + ">").queue();

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
            final String query = "SELECT * from USER_REGISTRATION WHERE id= '" + user.getId() + "'";
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
            privateChannel.sendMessage(WELCOME_Message + NAMA_Message).queue();
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

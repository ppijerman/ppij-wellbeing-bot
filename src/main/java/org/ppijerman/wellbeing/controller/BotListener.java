package org.ppijerman.wellbeing.controller;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;

public class BotListener extends ListenerAdapter {
    private static final String MAIN_CHANNEL = System.getenv("MAIN_CHANNEL");
    private static final String GUILD_ID = System.getenv("GUILD_ID");
    private static final String ROLE_ID = System.getenv("ROLE_ID");

    private static final Integer NAMA = 1;
    private static final Integer UMUR = 2;
    private static final Integer KESIBUKAN = 3;
    private static final Integer DOMISILI = 4;
    private static final Integer INSTITUSI = 5;
    private static final Integer MATA_KULIAH = 6;
    private static final Integer EMAIL = 7;
    private static final Integer ALASAN_HARAPAN = 8;

    private static final List<String> CITY_LIST = Stream.of(
            "Baden-Württemberg",
            "Bayern",
            "Berlin",
            "Brandenburg",
            "Bremen",
            "Hamburg",
            "Hessen",
            "Mecklenburg-Vorpommern",
            "Niedersachsen",
            "Nordrhein-Westfalen",
            "Rheinland-Pfalz",
            "Saarland",
            "Sachsen",
            "Sachsen-Anhalt",
            "Schleswig-Holstein",
            "Thüringen",
            "Indonesia",
            "Europe",
            "Others"
    ).toList();

    private static final List<String> OCCUPATION_LIST = Stream.of(
            "Studienkolleg (Foundation Year)",
            "Bachelor Student",
            "Master Student",
            "Ausbildung (Apprenticeship)",
            "Sprachkurs (Language Course)",
            "Housewife / Househusband",
            "Others (specify yourself directly)"
    ).toList();

    private final String WELCOME_MESSAGE;
    private final String NAME_MESSAGE;
    private final String UMUR_MESSAGE;
    private final String KESIBUKAN_MESSAGE;
    private final String DOMISILI_MESSAGE;
    private final String INSTITUSI_MESSAGE;
    private final String EMAIL_MESSAGE;
    private final String MATA_KULIAH_MESSAGE;
    private final String ALASAN_HARAPAN_MESSAGE;
    private final String THANKYOU_MESSAGE;

    private final DataSource ds;
    private final Logger log = LoggerFactory.getLogger(BotListener.class);
    private final HashMap<String, Object[]> userStageMap;
    private final HashMap<String, ScheduledFuture<?>> userTimer;
    private final ScheduledExecutorService userTimeoutScheduler;

    public BotListener(DataSource ds) {
        this.ds = ds;
        this.userStageMap = new HashMap<>();
        this.userTimer = new HashMap<>();
        this.userTimeoutScheduler = Executors.newScheduledThreadPool(1);
        this.WELCOME_MESSAGE = "Hi %s! Welcome to Wellbeing by PPI Jerman! Before you can start interacting with other members, let’s fill out this little questionnaire!\n";
        this.NAME_MESSAGE = "(1/7) Full Name:";
        this.UMUR_MESSAGE = "(2/7) Age:";
        this.KESIBUKAN_MESSAGE = createIndexedQuestionWithStartAndCounts(OCCUPATION_LIST, "(3/7) Occupation:", 1, 5);
        this.DOMISILI_MESSAGE = createIndexedQuestion(CITY_LIST, "(4/7) Where are you from?");
        this.INSTITUSI_MESSAGE = "(5/7) Institution Name:";
        this.MATA_KULIAH_MESSAGE = "(6/7) Study Field:";
        this.EMAIL_MESSAGE = "(7/7) E-Mail:";
        this.ALASAN_HARAPAN_MESSAGE = "Why are you interested in joining our community and what are your expectations?";
        this.THANKYOU_MESSAGE = "Great! Just one more thing to do. Please go back to our discord server and read the rules on our ‘rules’ channel and you’re all set!";
    }

    private static String createIndexedQuestion(List<String> list, String initMessage) {
        return createIndexedQuestionWithStartAndCounts(list, initMessage, 1, list.size());
    }

    private static String createIndexedQuestionWithStartAndCounts(List<String> list, String initMessage, int startIndex, int count) {
        AtomicInteger counter = new AtomicInteger(startIndex);
        return list.stream().reduce(initMessage, (acc, elem) -> {
            if (counter.get() - startIndex < count) {
                return acc + "\n" + counter.getAndIncrement() + ") " + elem;
            } else {
                return acc + "\n" + elem;
            }
        });
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore all bots messages
        if (event.getAuthor().isBot()) {
            return;
        }

        if ("!fill".equals(event.getMessage().getContentStripped())) {
            log.info("{} is initiated filling the form", event.getAuthor().getName());
            fillFormViaText(event.getAuthor());
            return;
        }

        if (userStageMap.get(event.getAuthor().getId()) == null) {
            return;
        }

        if (event.getChannelType() == ChannelType.PRIVATE) {
            event.getAuthor().openPrivateChannel().queue((privateChannel) -> {
                Object[] val = userStageMap.get(event.getAuthor().getId());
                Integer step = (Integer) val[0];
                String message = event.getMessage().getContentStripped();
                String id = event.getAuthor().getId();

                if (Objects.equals(step, NAMA)) {
                    val[step] = message;
                    val[0] = UMUR;
                    privateChannel.sendMessage(UMUR_MESSAGE).queue();
                } else if (Objects.equals(step, UMUR)) {
                    try {
                        int age = Integer.parseInt(message);

                        if (age > 0 && age <= 99) {
                            val[step] = age;
                            val[0] = KESIBUKAN;
                            privateChannel.sendMessage(KESIBUKAN_MESSAGE).queue();
                        } else {
                            privateChannel.sendMessage("Invalid age format (1 - 99)").queue();
                            privateChannel.sendMessage(UMUR_MESSAGE).queue();
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse number correctly on input: {}", message);
                        privateChannel.sendMessage("Cannot correctly parse your input, invalid age format (1 - 99)").queue();
                        privateChannel.sendMessage(UMUR_MESSAGE).queue();
                    }
                } else if (Objects.equals(step, KESIBUKAN)) {
                    try {
                        int occupation = Integer.parseInt(message);
                        // Other is not included
                        if (occupation > 0 && occupation <= OCCUPATION_LIST.size() - 1) {
                            val[KESIBUKAN] = OCCUPATION_LIST.get(occupation - 1);
                            val[0] = DOMISILI;
                            privateChannel.sendMessage(DOMISILI_MESSAGE).queue();
                        } else {
                            privateChannel.sendMessage("Your choice is not available in our list, please try again.").queue();
                            privateChannel.sendMessage(KESIBUKAN_MESSAGE).queue();
                        }
                    } catch (NumberFormatException e) {
                        val[KESIBUKAN] = message;
                        val[0] = DOMISILI;
                        privateChannel.sendMessage(DOMISILI_MESSAGE).queue();
                    }
                } else if (Objects.equals(step, DOMISILI)) {
                    try {
                        int cityNum = Integer.parseInt(message);
                        if (cityNum > 0 && cityNum <= CITY_LIST.size()) {
                            val[DOMISILI] = CITY_LIST.get(cityNum - 1);
                            val[0] = INSTITUSI;
                            privateChannel.sendMessage(INSTITUSI_MESSAGE).queue();
                        } else {
                            privateChannel.sendMessage("The number specified does not appear in the list, please make sure to choose from the list.").queue();
                            privateChannel.sendMessage(DOMISILI_MESSAGE).queue();
                        }
                    } catch (NumberFormatException e) {
                        privateChannel.sendMessage("Invalid input for your city, make sure to input only number.").queue();
                        privateChannel.sendMessage(DOMISILI_MESSAGE).queue();
                    }
                } else if (Objects.equals(step, INSTITUSI)) {
                    val[0] = MATA_KULIAH;
                    val[step] = message;
                    privateChannel.sendMessage(MATA_KULIAH_MESSAGE).queue();
                } else if (Objects.equals(step, MATA_KULIAH)) {
                    val[0] = EMAIL;
                    val[step] = message;
                    privateChannel.sendMessage(EMAIL_MESSAGE).queue();
                } else if (Objects.equals(step, EMAIL)) {
                    String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(message);
                    if (matcher.matches()) {
                        val[0] = ALASAN_HARAPAN;
                        val[step] = message;
                        privateChannel.sendMessage(ALASAN_HARAPAN_MESSAGE).queue();
                    } else {
                        privateChannel.sendMessage("Invalid Email Address").queue();
                        privateChannel.sendMessage(EMAIL_MESSAGE).queue();
                    }
                } else if (Objects.equals(step, ALASAN_HARAPAN)) {
                    if (!message.contains("submit")) {
                        val[ALASAN_HARAPAN] = message;
                    }

                    final String updateQuery = "UPDATE USER_REGISTRATION SET NAMA=? ," +
                            "UMUR=?, KESIBUKAN=?, DOMISILI=?, INSTITUSI=?, MATA_KULIAH=?," +
                            "EMAIL=?, ALASAN_HARAPAN=? WHERE id=?";
                    final String insertQuery = "INSERT INTO USER_REGISTRATION VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try (
                            Connection conn = ds.getConnection();
                            PreparedStatement updatePs = conn.prepareStatement(updateQuery);
                            PreparedStatement insertPs = conn.prepareStatement(insertQuery)
                    ) {
                        if (validateUser(event.getAuthor())) {
                            updatePs.setString(1, (String) val[1]);
                            updatePs.setInt(2, (Integer) val[2]);
                            updatePs.setString(3, (String) val[3]);
                            updatePs.setString(4, (String) val[4]);
                            updatePs.setString(5, (String) val[5]);
                            updatePs.setString(6, (String) val[6]);
                            updatePs.setString(7, (String) val[7]);
                            updatePs.setString(8, (String) val[8]);
                            updatePs.setString(9, event.getAuthor().getId());
                            updatePs.executeUpdate();
                        } else {
                            insertPs.setString(1, event.getAuthor().getId());
                            insertPs.setString(2, (String) val[1]);
                            insertPs.setInt(3, (Integer) val[2]);
                            insertPs.setString(4, (String) val[3]);
                            insertPs.setString(5, (String) val[4]);
                            insertPs.setString(6, (String) val[5]);
                            insertPs.setString(7, (String) val[6]);
                            insertPs.setString(8, (String) val[7]);
                            insertPs.setString(9, (String) val[8]);
                            insertPs.executeUpdate();
                        }
                        addRoleToUser(event, event.getAuthor().getId());
                        privateChannel.sendMessage(THANKYOU_MESSAGE).queue();
                    } catch (SQLException e) {
                        log.error("Cannot insert or update values in table with message: {}", e.getMessage());
                    }
                } else {
                    log.error("ERROR, weird Last Question Asked");
                }

                refreshUserTimeout(id);
            });
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getUser().isBot()) return;

        BaseGuildMessageChannel baseChannel = event.getGuild().getDefaultChannel();
        if (baseChannel != null) {
            baseChannel.sendMessage("Welcome <@" + event.getUser().getId() + ">").queue();
        }

        if (validateUser(event.getUser())) {
            addRoleToUser(event, event.getUser().getId());
            return;
        }

        fillFormViaText(event.getUser());
    }

    private boolean addRoleToUser(Event event, String id) {
        Guild guild = event.getJDA().getGuildById(GUILD_ID);
        if (guild != null) {
            Role role = guild.getRoleById(ROLE_ID);
            if (role != null) {
                guild.addRoleToMember(id, role).queue();
                return true;
            } else {
                log.error("Cannot find role with id: {}", ROLE_ID);
            }
        } else {
            log.error("Cannot find guild with id: {}", GUILD_ID);
        }
        return false;
    }

    private boolean validateUser(User user) {
        final String queryUser = "SELECT * FROM USER_REGISTRATION WHERE id = ?";

        try (
                Connection conn = ds.getConnection();
                PreparedStatement queryUserPs = conn.prepareStatement(queryUser)
        ) {
            queryUserPs.setString(1, user.getId());
            ResultSet rs = queryUserPs.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            log.error("Error on executing user validation with message: {}", e.getMessage());
        }

        return false;
    }

    private void fillFormViaText(User user) {
        Object[] val = new Object[9];
        val[0] = NAMA;

        userStageMap.put(user.getId(), val);
        user.openPrivateChannel().queue((privateChannel) ->
                privateChannel.sendMessage(String.format(WELCOME_MESSAGE, user.getName()) + NAME_MESSAGE).queue());

        setUserTimeout(user.getId());
    }

    private void setUserTimeout(String id) {
        final Runnable beeper = () -> {
            // TODO REMOVE USER HERE

            log.warn("Remove User: {} from HashMap due to inactivity", id);
        };
        userTimer.put(id, userTimeoutScheduler.schedule(beeper, 600, SECONDS));
    }

    private void removeUserTimeout(String id) {
        userTimer.get(id).cancel(true);
        if (userTimer.get(id).isCancelled()) {
            log.debug("The timer for user {} is not cancelled even though removeUserTimeout called!", id);
        }
        userTimer.remove(id);
    }

    private void refreshUserTimeout(String id) {
        removeUserTimeout(id);
        setUserTimeout(id);
    }
}

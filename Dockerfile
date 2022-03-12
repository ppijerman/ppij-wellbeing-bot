FROM eclipse-temurin:11-alpine
LABEL org.ppijerman.wellbeing-bot.vendor="PPI Jerman"
LABEL org.ppijerman.wellbeing-bot.title="Wellbeing Bot"
LABEL org.ppijerman.wellbeing-bot.licenses="MIT"
LABEL org.ppijerman.wellbeing-bot.ref.name="wellbeing-bot"
LABEL org.ppijerman.wellbeing-bot.athor="media@ppijerman.org"
LABEL org.ppijerman.wellbeing-bot.maintainer="media@ppijerman.org"
COPY target .
ENTRYPOINT ["java", "-jar", "wellbeing-bot.jar"]
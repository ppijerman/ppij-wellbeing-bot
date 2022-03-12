FROM eclipse-temurin:11
LABEL org.ppijerman.wellbeing-bot.athor="media@ppijerman.org"
LABEL org.ppijerman.wellbeing-bot.maintainer="media@ppijerman.org"
COPY target .
ENTRYPOINT ["java", "-jar", "app.jar"]
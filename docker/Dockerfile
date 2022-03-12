FROM openjdk:11-alpine
LABEL org.ppijerman.wellbeing-bot.athor="media@ppijerman.org"
LABEL org.ppijerman.wellbeing-bot.maintainer="media@ppijerman.org"
WORKDIR /app
COPY /build .
ENTRYPOINT ["java", "-jar", "app.jar"]
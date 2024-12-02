FROM eclipse-temurin:8u432-b06-jdk

ARG UID
ARG GID

ADD dist/snipsnap-1.1.tar.gz /dist
RUN mv /dist/snipsnap-1.1 /data

RUN chown -R ${UID}:${GID} /data
RUN chmod 777 /data/run.sh

EXPOSE 8668
WORKDIR /data

USER ${UID}

CMD ["/data/run.sh"]
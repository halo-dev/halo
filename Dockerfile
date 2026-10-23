FROM eclipse-temurin:21-jre AS builder

WORKDIR /application
ARG JAR_FILE=/application/build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:21-jre
LABEL maintainer="johnniang <johnniang@foxmail.com>"
WORKDIR /application
COPY --from=builder /application/extracted/dependencies/ ./
COPY --from=builder /application/extracted/spring-boot-loader/ ./
COPY --from=builder /application/extracted/snapshot-dependencies/ ./
COPY --from=builder /application/extracted/application/ ./

ENV JVM_OPTS="" \
    HALO_WORK_DIR="/root/.halo2" \
    SPRING_CONFIG_LOCATION="optional:classpath:/;optional:file:/root/.halo2/" \
    TZ=Asia/Shanghai

# --- BEGIN CVE-2026-91403 stopgap ---
# The private keys of eight eMudhra emSign intermediate CA certificates are
# recoverable from their public keys (https://emudhra.we-have-a-problem.com/),
# so an attacker can impersonate any TLS server. eclipse-temurin:21-jre
# currently resolves to 21.0.12.1, which still trusts the emSign roots and does
# not block the intermediates; Adoptium's newest 21 build is 21.0.13+9 and the
# fixed build is 21.0.13.0.2. Mirror the fixed JDK:
#   1. block the 16 impacted intermediate certificates by SHA-256. This is the
#      actual control: 8 keys, each with an eMudhra-issued and a Sectigo
#      cross-signed variant, so they stay untrusted even behind a trusted root.
#   2. drop emSign Root CA - C1, emSign ECC Root CA - C3 and emSign ECC Root
#      CA - G3, matching Mozilla's NSS 3.131.1 removal (JDK 21.0.13 already
#      does this). emSign Root CA - G1 is unaffected and stays trusted.
# Delete this whole block once the base image ships 21.0.13.0.2 or later.
RUN set -eu; \
    security="$JAVA_HOME/lib/security"; \
    blocked="$security/blocked.certs"; \
    listing="$security/emudhra-cacerts.txt"; \
    [ -f "$blocked" ] || { echo "CVE-2026-91403 stopgap: $blocked is missing" >&2; exit 1; }; \
    blocked_before=$(grep -cE '^[0-9A-F]{64}$' "$blocked" || true); \
    for fingerprint in \
        42C4ACCD12C4F59597925E1A08A7A90102608543809308D50EC427E118BA1493 \
        45A883F349AEDFD5F3A649B5498FFB121712BA8B448F3F2A91DBC5A766FCDDBE \
        98B896625C0C230ADEEC3EB2CF6AAD5001ECF9156A68644E82374CE5833BC25B \
        5F1DED4DD590AF74631290CF073B122A2AAC6A1FA479778A0BD43579BD832699 \
        E7568FE6E12331F4FD7DE61DA4C7559D0F8EB192A218D4741A3BA75184333838 \
        579480AEA166290BD3C643589E795FC0D0E54E86505D180F1DFF99EFD3199512 \
        0164D7CEAE5BB909C272AF151FF44B1177AE9B47D9424B94F1D9558C30934D19 \
        62B7F9868263F72F6911FC78CEAF72C1979158B7368BF648683BB2AA39B02E72 \
        CB6E15165AC802E9B3F993F16C25D41F4E81C21EBDE71AE1B012AC00BF9C7C3F \
        7485B51C0ED192878FA1D2F948FB529AE1842CC5086C4B67F7BE5B2039DE9489 \
        432EC25F0D28DE9006D97BF752F17BBB2CD41F669933FCB072D9A1148D0261F3 \
        FD9D5C2DBBD4995AB69B8D50C439D9527B00529199C2FD01B2174EB58F07021F \
        9E39DE2881273ACC225A149C72BA3E0F51424963669AB633EE1DC7DF5DB067B4 \
        A496E23AC0CAB8155FC9B117B97EAE368EFCBB9041AEC8804D5A308D2D01C2D8 \
        4B2460A466F95FD40113BCDCCA2CEB29B3B093921DE45474DA7DF48874C630B1 \
        84112972B8E59D14560AE7E7119654474AAD119990CF3722BFC1E3B91615E1FD; do \
        grep -qx "$fingerprint" "$blocked" || printf '%s\n' "$fingerprint" >> "$blocked"; \
    done; \
    blocked_after=$(grep -cE '^[0-9A-F]{64}$' "$blocked"); \
    keytool -list -v -cacerts -storepass changeit > "$listing"; \
    awk '/^Alias name: /{alias=substr($0,13)} \
           /^Owner: CN=emSign (Root CA - C1|ECC Root CA - C3|ECC Root CA - G3),/{print alias}' \
        "$listing" > "$listing.remove"; \
    while IFS= read -r alias; do \
        keytool -delete -cacerts -storepass changeit -alias "$alias"; \
    done < "$listing.remove"; \
    rm -f "$listing" "$listing.remove"; \
    trusted_emsign=$(keytool -list -cacerts -storepass changeit | grep -ci emsign || true); \
    test "$trusted_emsign" -eq 1 || { \
        echo "CVE-2026-91403 stopgap: expected only emSign Root CA - G1 to remain, found $trusted_emsign emSign roots" >&2; \
        exit 1; \
    }; \
    echo "CVE-2026-91403 stopgap applied: $((blocked_after - blocked_before)) fingerprints added, $blocked_after blocked in total"
# --- END CVE-2026-91403 stopgap ---

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone

RUN java -XX:ArchiveClassesAtExit=application.jsa -Dspring.context.exit=onRefresh -jar application.jar --halo.work-dir=/tmp/halo2 \
    && rm -rf /tmp/halo2

EXPOSE 8090

ENTRYPOINT ["sh", "-c", "exec java ${JVM_OPTS} -XX:SharedArchiveFile=application.jsa -jar application.jar \"$@\"", "--"]

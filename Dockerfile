FROM eclipse-temurin:21@sha256:92a2a4d7a928d057e7bd999c418d66c26a34eb9a0442f3ab67721c3f88110b2d

ARG IMAGE_REVISION="0000000000000000000000000000000000000000"
ARG IMAGE_CREATED="1970-01-01T00:00:00Z"

# EUPL-1.2 (Art. 5): this image ships a modified version of SIMPL edcconnectoradapter. The licence,
# the third-party notices and the modification notice travel with the image, and the labels below
# point to the repository where the complete corresponding source code is available.
LABEL org.opencontainers.image.title="edc-connector-adapter (CNIE-ES fork)" \
      org.opencontainers.image.description="Modified version of SIMPL edcconnectoradapter (upstream commit 0a9cd20), modified by the EDNEL-RIOJA project team for CNIE-ES between 2026-05-04 and 2026-09-10. See /licenses/NOTICE.EDNEL.md." \
      org.opencontainers.image.version="1.14.1-edval" \
      org.opencontainers.image.vendor="CNIE-ES" \
      org.opencontainers.image.licenses="EUPL-1.2" \
      org.opencontainers.image.source="https://github.com/cnie-es/simpl-edc-connector-adapter" \
      org.opencontainers.image.revision="${IMAGE_REVISION}" \
      org.opencontainers.image.created="${IMAGE_CREATED}"

RUN groupadd -g 1001 simplgroup && useradd -u 1001 -g simplgroup -m simpluser

WORKDIR /home/simpluser

#copy the pipeline.variables.sh file for return microservice version in /status endpoint
COPY pipeline.variables.sh .

# The release/build.sh hook produces the artifact, built from ${revision} = the release tag,
# so the jar declares the same version the image is tagged and labelled with.
# The resulting absolute jar path must be also configured in values.yaml (see appJarPath)
COPY target/edc-connector-adapter.jar app.jar

# The notices must travel with every copy of the Work (EUPL-1.2, Art. 5).
COPY LICENSE NOTICE NOTICE.json NOTICE.EDNEL.md THIRD_PARTY_LICENCES.md THIRD_PARTY_LICENCES.xml /licenses/

RUN chown -R simpluser:simplgroup /home/simpluser

USER simpluser

ENTRYPOINT ["java", "-jar", "/home/simpluser/app.jar"]

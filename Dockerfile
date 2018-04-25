FROM java:8-jdk
MAINTAINER Shisei Hanai<shanai@jp.ibm.com>

RUN useradd -d "/var/home" -s /bin/bash devuser
RUN mkdir -p /opt/devsite
ADD target/universal /opt/devsite

RUN cd /opt/devsite && \
  cmd=$(basename *.tgz .tgz) && \
  tar xf $cmd.tgz

RUN cd /opt/devsite && \
  cmd=$(basename *.tgz .tgz) && \
  echo "#!/bin/bash -xe" > launch.sh && \
  echo printenv >> launch.sh && \
  echo "ls -lh /opt/devsite" >> launch.sh && \
  echo 'kill -9 `cat /opt/devsite/$cmd/RUNNING_PID`' && \
  echo rm -f /opt/devsite/$cmd/RUNNING_PID >> launch.sh && \
  echo /opt/devsite/$cmd/bin/formbuildersite -J-Xmx512m -DmoduleName=$cmd -Dplay.crypto.secret=\${APP_SECRET} -Dplay.evolutions.db.default.autoApply=true >> launch.sh && \
  chmod +x launch.sh

RUN chown -R devuser:devuser /opt/devsite
USER devuser

EXPOSE 9000

ENTRYPOINT ["/bin/bash", "-c", "/opt/devsite/launch.sh"]
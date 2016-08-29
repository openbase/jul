#mvn clean deploy -Pdeploy,sonatype --settings .travis/settings.xml -DskipTests=true -B -U
mvn clean deploy -Pdeploy,sonatype --settings .travis/settings.xml -DskipTests=true -U

#!/bin/bash
set -ev
echo trigger deployment
mvn clean deploy -Pdeploy,sonatype --settings .travis/settings.xml -DskipTests=true -B -U

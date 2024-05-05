#!/bin/bash
mvn org.codehaus.mojo:license-maven-plugin:2.4.0:update-file-header -Dlicense.trimHeaderLine=true -Dlicense.encoding=utf-8 -Dlicense.inceptionYear=2024 -Dlicense.organizationName="Michael I. Calderero" -Dlicense.canUpdateDescription=true -Dlicense.licenseName=apache_v2

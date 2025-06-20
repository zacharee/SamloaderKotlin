#!/bin/sh

# https://stackoverflow.com/questions/72578619/building-kotlin-multiplatform-app-with-xcode-cloud
# https://developer.apple.com/forums/thread/720137?answerId=741461022#741461022

brew install cocoapods

curl -s "https://get.sdkman.io" | bash
. "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.7-ms

cd ../../ && chmod +x gradlew && ./gradlew common:podInstall

cd iosApp/ci_scripts || exit 100

pod install

# Actually has to be set in the Environment Variables section in Xcode Cloud
export JAVA_HOME=/Users/local/.sdkman/candidates/java/current

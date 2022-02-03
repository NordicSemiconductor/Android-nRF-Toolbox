fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew install fastlane`

# Available Actions
## Android
### android test
```
fastlane android test
```
Runs all the tests
### android beta
```
fastlane android beta
```
Submit a new Beta Build to Crashlytics Beta
### android deploy
```
fastlane android deploy
```
Deploy a new version to the Google Play
### android deployNexus
```
fastlane android deployNexus
```
Deploy libraries to Nexus.
### android deployAlpha
```
fastlane android deployAlpha
```
Deploy build to Alpha channel.
### android deployBeta
```
fastlane android deployBeta
```
Deploy build to Beta channel.
### android deployInternal
```
fastlane android deployInternal
```
Deploy build to internal channel.
### android deployInternalLink
```
fastlane android deployInternalLink
```
Generate internal Play Store link to the apk

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).

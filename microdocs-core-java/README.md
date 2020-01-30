# MicroDocs core Java
Core functionaries used across other MicroDocs Java projects

## Build
```
$ gradle jar
```

## Publish to Maven Central
1. Create a [sonatype account](https://issues.sonatype.org/secure/Signup!default.jspa) and create an issue to request access to com.maxxton
2. Install [GnuPG](https://www.gnupg.org/download/) NOTE: at least version 2.1!
3. Generate key pair by running:
```
$ gpg --full-generate-key
```
choose RSA 2048bit with no expiration.
Your key is saved in ~/.gnupg
4. Run this to update your secring.gpg:
```
$ gpg --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg
```

5. Create ```gradle.properties``` in the gradle home folder (~/.gradle)
~/.gradle/gradle.properties
```
signing.keyId=publickeyid
signing.password=yourpassword
signing.secretKeyRingFile=C:\\Users\\username\\.gnupg\\secring.gpg

sonatypeUsername=username
sonatypePassword=password
```
Fill in these properties correctly.
Use 'gpg --list-secret-keys --keyid-format 0xshort' to get your keyId

6. Publish
```
$ gradle uploadArchives
```

And follow the [release and deployment manual](https://central.sonatype.org/pages/releasing-the-deployment.html)
Check this link for more info on [the signing plugin](https://docs.gradle.org/current/userguide/signing_plugin.html) 

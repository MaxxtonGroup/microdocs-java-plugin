# microdocs-java-plugin
This is the Java plugin to publish data to your [microdocs server](https://github.com/MaxxtonGroup/microdocs). For more info, see the three READMEs's in the subfolders.


## Publishing to Maven Central

Before you can publish, you'll have to do some setup.

1. Create a [sonatype account](https://issues.sonatype.org/secure/Signup!default.jspa) and create an issue to request access to com.maxxton
2. Install [GnuPG](https://www.gnupg.org/download/) NOTE: at least version 2.1!
3. Generate key pair by running:
```
$ gpg --full-generate-key
```
Choose RSA 2048bit with no expiration. Fill in your name, email, and passphrase. Your key is saved in `~/.gnupg`.


4. Run this to update your secring.gpg:
```
$ gpg --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg
```

5. Create `~/.gradle/gradle.properties` (or `C:\\Users\\username\\.gradle\\gradle.properties`) with the following properties:
```
signing.keyId=keyid
signing.password=yourpassword
signing.secretKeyRingFile=C:\\Users\\username\\.gnupg\\secring.gpg

sonatypeUsername=username
sonatypePassword=password
```

Use `gpg --list-secret-keys --keyid-format 0xshort` to get your keyId
```
% gpg --list-secret-keys --keyid-format 0xshort
[keyboxd]
---------
sec   rsa2048/0x1337CAFE 1970-01-01 [SC]
              ^^^^^^^^^^
              ## this bit is what you're looking for.
      1337133713371337133713371337133713371337
uid           [ultimate] Your Name <y.name@maxxton.com>
ssb   rsa2048/0xCAFE1337 1970-01-01 [E]
```

6. Publish
```
$ ./publish
```

And follow the [release and deployment manual](https://central.sonatype.org/pages/releasing-the-deployment.html)
Check this link for more info on [the signing plugin](https://docs.gradle.org/current/userguide/signing_plugin.html)


### Java 8
Checkout the java8 branch for a Java 8 compatible version. The master branch will be based on Java 11, the difference is substantial due to the revised Javadoc API in since Java 9.


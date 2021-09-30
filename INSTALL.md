
# CFT Install on Windows

## Open JDK

### Download 

https://jdk.java.net/17/

Select zip-file for windows. Download and unpack into some local directory,
for example under c:\software

### Environment variables

Add path to the bin-directory inside the JDK directory to Path environment variable.

Set JAVA_HOME to point to the JDK directory (not the bin-dir).

Start new CMD to get environment variables updated.

### Test

```
java -version
```

## Maven on windows

### Download

https://maven.apache.org/download.cgi

Select zip-file for windows. Download an unpack into local directory, 
for example under c:\software

Requires Path to point to java bin-directory, as well as JAVA_HOME as defined above.

### Test

```
mvn -version
```


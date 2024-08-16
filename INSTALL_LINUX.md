
# CFT Install on Linux

### Java and maven


Requires openjdk version 17 or newer.

```
sudo apt-get install openjdk-17-jdk-headless
sudo apt-get install maven
```


### Clone from Github

Git should already be present on all Linux installs.

```
git clone https://github.com/rfo909/CFT.git
```

### Build with maven

```
cd CFT
mvn clean package
```


### Run

```
./cft
```

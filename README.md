
# CFT ("ConfigTool")


CFT is a terminal based Java application, and a programming language, created to interactively
build and run code for all kinds of automation.

Create configuration files, install software, copy files, search logs.


# Download and compile

Written in Java and built using Apache ANT, which results in a single JAR file. 
Runs on both Linux and 
Windows, and has no dependencies outside of standard Java libraries.


Once built, the application is started 

```
./cft
```

On Windows, run

```
.\cft.cmd
```

To leave type ":quit" or just type CTRL-C.



# Introduction

An object oriented DSL for automation.


Example:

```
# Define a list of hosts
# --
List("host1","host2","host3")
/DebianHosts

# Run update and upgrade on debian based hosts
# --
commands=Sequence(
	@ apt-get update
	@ apt-get -y upgrade
)
DebianHosts->host 
	x = SSH:run(host, commands, true)
	collection = Sys.savefile.name.beforeLast(".")
	if (x.exitCode != 0) {
		Db:Add(host,x,collection+"_failed")
	} else {
		Db:Add(host,Date,collection+"_ok")
	}
/AptUpdateUpgrade

# Identify files changed last N days
# --
	P(1,31) => days
	Date.sub(Date.Duration.days(days)).get => limit
	Dir.allFiles->f 
		assert(f.lastModified > limit) 
		report(f.path, Date(f.lastModified).fmt)
/RecentlyChangedFiles

```



# An interactive environment


```
$ @term
  <obj: Cfg>
  24x72 :wrap=false


$ ?SSH:
+-----------------------------------------------------
| Readme       : # Readme
| ReadSSHTarget: Input("SSH target on format username@server").get
| remoteShell  : # Open ssh connection to target
| sshEnable    : # Copy ssh key to remote target host
| HostOk       : # Check if server responds on ping
| HostOkSSH    : # Check if ok with SSH
| VerifySudo   : # Returns boolean indicating if sudo without password +
| TmpFile      : # Create name of temp file under /tmp
| run          : # Run single or multiple commands (string or list) on +
| sudo         : # Run single or multiple commands (string or list) as +
| copy         : # Copy local file via scp (secure copy over SSH) to re+
+-----------------------------------------------------



$ ?SSH:run
+-----------------------------------------------------

## Run single or multiple commands (string or list) on remote target via+
#
P(1) => target 
P(2) => commands
P(3,false) => acceptErrors
P(4,false) => showDebug

    error(target==null || !target.contains("@"), "Invalid target: '" + +
    error(commands==null, "Invalid commands: can not be null")

	:


$ cd ../project1/src
$ RecentlyChangedFiles(7)
    
    :
```


# More documentation

[Detailed documentation](doc/Doc.md).


# Goals

- Interactive programmable shell
- Compact programming language
- Automation tool



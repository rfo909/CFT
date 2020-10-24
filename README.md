

# CFT (ConfigTool)

Configurable personal shell, automation tool.

## Example script file

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

# Interactive use


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
$ ls
$ more somefile.txt

$ RecentlyChangedFiles(7)
    
    :
```

# What it is

An object oriented DSL for automation.

A terminal based Java application, and an interpreted programming language.

Create configuration files, install software, copy files, search logs.


# Download and compile

Written in Java and built using Apache ANT, which results in a single JAR file. 

Tested on both Linux and Windows. Has no dependencies outside of standard Java libraries.


Linux: 

```
./cft
```

Windows:

```
.\cft.cmd
```

To leave type ":quit" or just type CTRL-C.



# Goals

- Interactive programmable shell
- Compact programming language
- Automation tool



[Detailed documentation](doc/Doc.md).



# CFT (ConfigTool)

Configurable personal shell, automation tool.

## Example script file


```
"s01.s s02.s s03.s s04.s s05.s s06.s s07.s".split
/Hosts

# Ping one host, return boolean (true if ok)
# --
	P(1)=>host

	println("Pinging host " + host)
	Lib:run(List("ping","-c","1",host), List, true) => res
	
	if (res.exitCode==0) {
		true
	} else {
		println("FAILED with exitCode=" + res.exitCode)
		Inner {
			res.stdout->line println(line) |
			res.stderr->line println("#ERR# " + line) |
		 }
		 false
	}
/ping


# Delete previous ping stats, then run ping on all
# hosts as parallel processes, collecting results and store in 
# database.
# --
	COLLECTION="stats"
	Db2Obj:DeleteObjects(COLLECTION, Lambda{P(1).value.op=="ping"})

	Hosts->host
		data=Dict.set("host",host)
		proc=SpawnProcess(data,ping(host))
		out(proc)
	| _ => runningProcesses

	runningProcesses->proc
		proc.wait

		dbObj=Dict
			.set("op","ping")
			.set("host", proc.data.host)  # the data dict from SpawnProcess
			.set("ok",proc.exitValue)
		if (proc.exitValue==false) {
			# failed
			dbObj.set("output", proc.output)
		}
		# Log everything to database collection stats
		Db2Obj:AddObject(COLLECTION, dbObj)
/CheckPing

# To verify database content
#
# $ Db2Obj:ShowFields("stats",List("host","ok"))
#  <List>
#  	0: 2020-11-05 22:48:44 | s01.s | true
#   1: 2020-11-05 22:48:44 | s02.s | true
#   2: 2020-11-05 22:48:44 | s03.s | true
#   3: 2020-11-05 22:48:47 | s04.s | false
#   4: 2020-11-05 22:48:47 | s05.s | true
#   5: 2020-11-05 22:48:47 | s06.s | true
#   6: 2020-11-05 22:48:47 | s07.s | false
#
# To see output from s04.s
#
# $ Db2Obj:FindObjects("stats",Lambda{P(1).value.host=="s04.s"}).first.value.output
#  <List>
#   0: Pinging host s04.s
#   1: FAILED with exitCode=1
#   2: PING s04.s (10.0.11.41) 56(84) bytes of data.
#   3: From 10.0.0.84 (10.0.0.84) icmp_seq=1 Destination Host Unreachable
#   4: 
#   5: --- s04.s ping statistics ---
#   6: 1 packets transmitted, 0 received, +1 errors, 100% packet loss, time 0ms
#   7: 

```

### Topics in the code

- Lists, dictionaries, strings
- Functions, function parameters
- List iterations, conditionals
- External programs, parallel processes, lambdas
- CFT's integrated database

## Interactive use


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

A personal interactive shell.

# What it does

- Install software
- Create config files
- Copy, delete, move files and directories
- Search logs
- Remote management with SSH and Powershell
- Built-in help system
- Multithreaded

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

To leave type ":quit" or just CTRL-C.



# Goals

- Interactive programmable shell
- Compact programming language
- Automation tool



[Detailed documentation](doc/Doc.md).

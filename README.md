
# Automation at all levels

CFT is short for *ConfigTool*. 

It is an interpreted and interactive shell and programming language. It was initiated
because of a need for a scripting tool in my job as a developer, and from my 
interest in parsers and interpreters.

*README last updated 2021-10-27*

## Terminal based - shell-like - programmable

The command line makes CFT feel like a shell, for navigating the directory tree, and inspecting files,
using the following commands.

- cd, ls, pwd, cat, more, edit

However, CFT is really about creating and running *functions*.

## Functions

In CFT the code comes before the function name. The P(N) global function returns parameters by position.

```
# Example
# --
	file1=P(1)  # Expects File objects
	file2=P(2)
	file1.hash==file2.hash # boolean return value
/FilesMatch
```

*Note* that the two ".hash" are function calls on the File objects received as parameters.
 
Parantheses are optional when no arguments. 

When we create our own functions, they are organized into script files. They may call each other, both inside the
same script file, and in other script files, as well as member functions inside library objects, such as
the calls to File.hash() above. 

Scripts contain no state, and are just a way of organizing code, making each script essentially a name space. 

- 70+ library object types
- 390+ library functions

About 30 of the library functions are global, the rest exist inside different object types.

## Functionality

- shell-like command line interface / REPL
- create functions, do interactive testing, use interactive help
- lists and dictionaries
- run external programs in foreground or background
- (inline) text templating with merge code processing
- spawn CFT expressions as background threads
- lambdas and closures
- tryCatch with two-tiered exception hierarchy ("soft" and "hard")
- integrated data store (Db2) 
- integrated lexer; JSON recursive-descent parser written in CFT

### Editing script code

Originally, the idea was to build code from the bottom up, one line at a time, interactively,
but nowadays we usually edit script code in some editor. 

The shortcut @e opens current script file to be edited in notepad or notepad++ on windows, and 
whatever preferred editor is selected on Linux.


### Documentation

The documentation is extensive, and kept up-to-date. There also is a Youtube tutorial, plus
another playlist with shorter "howto"-videos.


### Shortcuts

Frequently used commands or command sequences can be stored as shortcuts. These are defined in
the CFT.props file, and by default include:

```
@e       - open current script in editor
@fm      - open file manager for current dir
@home    - move to script directory
@c       - copy selection of files to clipboard, to be copied on @v
@x       - copy selection of files to clipboard, to be moved on @v
@v       - paste selection of files to current dir
```

### Protection mechanism

CFT has a built-in directory and file *protection*, which may help us avoid modifying critical data on live
systems, such as database files, logs, etc. 

Read more about it in the docs, or view the Youtube tutorial video [episode six](https://www.youtube.com/watch?v=7e-f1gudxpE&list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO&index=7).


## Object oriented - functional

### Values are objects - member functions

All values in CFT are objects. These in turn have member functions, which we call to either
modify the value, or get information from it, etc.

### Global functions

There also is a set of global functions, which return values, such as the current
directory, an empty dictionary, a list, the current date, and so on. 

All values are objects, and there are no primitive types in the classic meaning. Example:

```
$ "test".length
  <int>
  4

$ 23.bin
  <String>
  00010111

$ "abc".chars.reverse.concat
  <String>
  "cba"
```

### Inspired by PowerShell

CFT was inspired by PowerShell, working with objects instead of strings, as in bash. 

Apart from a couple of "peculiarities", CFT strives for a regular and predictable syntax, 
compared to PowerShell and bash. 

The interactive approach made possible a help system, where one can always run some expression, and list 
member function of the result.



## No global state

To keep the language simple, CFT *does not support* user defined classes, only user defined functions.

This in turn correlated well with making the language as stateless as possible, for multiple reasons, 
most generally robustness, but also enabling safe multi-threading. 

CFT has no global variables, and there is no script state. A script in CFT is a collection of related
functions, nothing more. 

There are options for storing data, either to file, or using the integrated Db2 data store.

The goal is to minimize unwanted side effects.


## Just code

CFT implements object types for lists and dictionaries, and the Db2 data store is able to save most data
structures, using a special mechanism called *synthesis*, which produces code from values. This code on
string format is stored when saving data with Db2, or it can be written to some file.

When loading data from Db2, the code is run through eval() which recreates the original data.


## Frequent uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks
- automate powershell command sequences

It's been in daily use since 2019 in my work as a software developer, and is stable. 



## Example: ping some hosts

```
# Readme function
# --
<<< EOF
Check if hosts respond to ping
    (The arrow plus identifier is a foreach)
    (The  /name lines define functions from the preceding code lines)
>>> EOF
/Readme

# Define hosts
# --
	List("host1","host2","host3")
/hosts


# Create report
# --
	hosts->host report(host,SSH:HostOk(host))
/checkPing 
```

### Rewritten to do parallel pings

Since pinging hosts that don't respond may take a while, we decide to run
all pings in parallel, then collect information. 

Total time is then the time of the single slowest ping, not the sum of times for all pings.

Threads are created by calling SpawnProcess() with a dictionary for local
variables, and an expression. This immediately returns Process objects, which
are collected in a list via the out() statement.

When all processes have been spawned, we loop through the processes, wait for
each to terminate, then report its result. 

```
# Create report
# --
	hosts->host 
		out(SpawnProcess(SymDict(host), SSH:HostOk(host)))
	| ->proc
		println("Waiting for " + proc.data.host)
		proc.wait
		report(proc.data.host, proc.exitValue)
/checkPing 
```

### The SSH:HostOk function

Above we're calling the HostOk function inside the SSH script. It is implemented as follows.

```
# Check if server responds on ping
P(1) =>target
    if(target.contains("@"), target.after("@"), target) =>host
	Lib:run(List("ping","-c","1",host),List,true).exitCode => ex

	ex == 0
/HostOk
```

It in turn calls function "run()" inside Lib script, which eventually ends up doing a call 
to Dir.runProcess() which actually runs the external program. The details don't matter so much
as the concept of creating a hierarchy of functions with no (or very few) side effects, providing
high level reliability, such as the HostOk function.

## Interactive help

Type "help" lists all global functions. 

```
$ help
  # v2.9.0
  # 
  # _Expr() - display information about expressions in CFT
  # _Stmt() - display information about Statements in CFT
  # 
  # Binary(hexString) - convert hex string to Binary value
  # DataFile(file,prefix) - create DataFile object
  # Date(int?) - create Date and time object - uses current time if no parameter
  # Dict() - create Dict object
  # Dir(str?) - creates Dir object
  # File(str) - creates File object
     :
     :
```

Note two special functions starting with underscore, which
provide info in built-in statements and expressions when run, like this:

```
$ _Stmt
  # 
  # Statements in CFT
  # -----------------
  # 
  # Looping and iteration over lists:
  #    loop ... break(cond)
  #    list -> variable ...
  # 
  # Loop control:
  #    assert (boolExpr)
  #    reject (boolExpr)
     :
     :

$ _Expr
  # 
  # Expressions in CFT
  # ------------------
  # 
  # Logical
  #    bool || bool
  #    bool && bool
  # 
  # Compare
  #    >  <  >=  <= == !=
     :
     :
```


To list functions inside an object, such as string, we type:

```
$ "" help
  # after(str) - return string following given string
  # afterLast(str) - return string following last position of given string
  # before(str) - return string up to given string
  # beforeLast(str) - return string up to last position of given string
  # between(pre,post) - return string between two given strings
     :
     :
```




# Download and compile

Written in Java and built with Maven, which results in a single JAR file. 

Tested on both Linux and Windows. 



```
git clone https://github.com/rfo909/CFT.git
cd CFT
mvn package
./cft
$ 2+3
5
```


[Detailed walkthrough for Windows](INSTALL_WINDOWS.md).


# References

[Full documentation](doc/Doc.md).

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO).

[Youtube HOWTO-videos](https://www.youtube.com/playlist?list=PLj58HwpT4Qy-12WjM16ALnLGEyy3kxX9r).

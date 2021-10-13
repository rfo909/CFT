
# Automation at all levels

CFT is an interpreted and interactive shell and programming language. It was initiated
because of a need for a do-all tool in my job as a developer, and from my long lasting
interest in parsers and interpreters.

*README last updated 2021-10-13*

## Terminal based - shell-like - programmable

The REPL makes it CFT work like a shell, for navigating the directory tree, and inspecting files:

- cd, ls, pwd, cat, more, edit

However, CFT is really about creating and running *functions*.

## Functions

In CFT the code comes before the function name.

```
# Example
# --
	file1=P(1)
	file2=P(2)
	file1.hash==file2.hash # boolean return value
/FilesMatch
```


Functions are collected in script files, and can call each other, as well as functions in
other scripts, and functions inside library objects. 

- 70+ library object types
- 390+ library functions

About 30 of the library functions are global, the rest exist inside different object types.

## Functionality

- shell-like command line interface / REPL
- create functions, do interactive testing
- interactive help system
- lists and dictionaries
- run external programs in foreground or background
- (inline) text templating with merge code processing
- spawn CFT expressions as background threads
- lambdas and closures
- tryCatch with two-tiered exception hierarchy ("soft" and "hard")
- integrated data store (Db2) 
- integrated lexer; full JSON recursive-descent parser written in CFT

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

CFT has a built-in protection mechanism that may help us avoid modifying critical data on live
systems, such as database files, persistens logs etc. Read more about it in the docs, or view
the Youtube tutorial video [episode six](https://www.youtube.com/watch?v=7e-f1gudxpE&list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO&index=7).

## Object oriented - functional

CFT consists of a set of global functions, which return values, such as the current
directory, an empty dictionary, some list, the current date, and so on. All values are objects,
and so in turn have inner functions that we can call. 

There are no primitive types, all are objects:

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

The key concept was to create an *interactive and interpreted* language inspired by
PowerShell, working with objects instead of strings, as in bash, but with a more regular syntax.

To keep the language simple, CFT *does not support* user defined classes, only user defined functions.

Functions are stored in script files, which are really just name spaces. Functions can of course
call each other, both inside a script and in other scripts. 


## No global state

CFT has no global variables, no script state, unless using files or the integrated Db2 data store. 

This minimizes unwanted side effects. 


## Frequent uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks
- automate powershell command sequences

It's been in daily use since 2019 in my work as a software developer, and is stable. 



## Example: ping hosts

```
# Ex: check if hosts respond to ping
#    (The arrow plus identifier is a foreach)
#    (The  /name lines define functions from the preceding code lines)

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

Since pinging hosts that don't respond takes a while, we may want to run
all pings in parallel, then collect information. Total time is then the
time of the single slowest ping, not the sum of times for all pings.

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

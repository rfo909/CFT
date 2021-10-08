
# Automation at all levels

CFT is an interpreted and interactive shell and programming language. It was initiated
because of a need for a do-all tool in my job as a developer, and from my long lasting
interest in parsers and interpreters.

*README last updated 2021-10-08*

## Terminal based - shell-like - programmable

The REPL makes it act like a shell, for navigating the directory tree, and inspecting files:

- cd
- ls
- pwd
- cat 
- more
- edit

However, CFT is really about creating and running *functions*.

## Functions

Functions are collected in script files, and can call each other, as well as functions in
other scripts. The system library consists of some 70+ object types, each
with member functions, for a total of some 370+ library functions (about 30 of them are global).

## Functionality

- shell-like command line interface / REPL
- create functions, do interactive testing
- integrated help system
- lists and dictionaries
- run external programs
- spawn CFT expressions as background threads
- lambdas and closures
- text templating with merge code processing
- tryCatch with two-tiered exception hierarchy ("soft" and "hard")
- integrated database storing complex data structures (lists, dictionaries etc)
- integrated encryption 
- integrated lexer; JSON parser implemented as CFT script
- protection mechanism for files and directories
- extensive and up-to-date documentation + Youtube videos

Originally, the idea was to build code from the bottom up, one line at a time, interactively,
but nowadays we usually edit script code in some editor. 

The shortcut @e opens current script file to be edited in notepad or notepad++ on windows, and 
whatever preferred editor is selected on Linux.

## Shortcuts

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

## Protection mechanism

CFT has a built-in protection mechanism that may help us avoid modifying critical data on live
systems, such as database files, persistens logs etc. Read more about it in the docs, or view
the Youtube tutorial video [episode six](https://www.youtube.com/watch?v=7e-f1gudxpE&list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO&index=7).

## Object oriented - functional

CFT consists of a set of global functions, which return values, such as the current
directory, an empty dictionary, or the current date, and so on. All values are objects,
and so in turn have inner functions that we can call. 

There are no primitive types, all are objects:

```
$ "test".length
  <int>
  4

$ 23.bin
  <String>
  00010111
```

The key concept was to create an *interactive and interpreted* language inspired by
PowerShell, working with objects instead of strings, as in bash, but with a more regular syntax.

To keep the language simple, CFT *does not support* user defined classes, only user defined functions.

Functions are stored in script files, which are really just name spaces. Functions can of course
call each other, both inside a script and in other scripts. 


## No global state

CFT has no global variables, no script state. This minimizes unwanted side effects. 


## Frequent uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks
- automate powershell command sequences

It's been in daily use since 2019 in my work as a software developer, and is stable. 



## Example

```
# Ex: check if hosts respond to ping
#    (The arrow plus identifier is a foreach)
#    (The  /name lines define functions from the preceding code lines)

List("host1","host2","host3")
/hosts

hosts->host report(host,SSH:HostOk(host))
/checkPing 
```


## Interactive help

Typing "help" lists all global functions. 

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


## Motto

*Unless you script it, (and check it in) it isn't real*

Manual operations are boring, risk errors, hard to keep documented, and should be avoided. 



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

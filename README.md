
# CFT ("ConfigTool")


CFT is a shell-like terminal based Java application, and a programming language, created to interactively
build code for all kinds of automation: creating configuration files, copying files, searching through
logs and running external programs to start/stop services, etc.


# Download and compile

The project is written in Java and built using Apache ANT, which results in a single JAR file. 
It runs on both Linux and 
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

The CFT application supports basic shell functions like "cd", "ls" and "pwd", but
is primarily a Domain Specific Language (DSL) for automation.

It is object oriented, as opposed to traditional -ix and -ux shells such as bash, which process text.

Example:

```
$ Dir.files.length

# means: 
# - call global function Dir() with no parameters
# - returns Dir object for current directory
# - call .files() inside the Dir object 
# - returns list of File objects
# - call .length() function on the list object
# - returns an int
```



# An interactive language

CFT reads single lines from the user, and as we press Enter, the line is interpreted. 

There is full expression support, with normal cardinality rules, so CFT can be used as
a desk calculator. Interactivity makes it easy to experiment.

```
$ 2+3*5
$ "a b c d".split
$ List(1,2,3).concat("x")
$ ls
$ cd ..
```


# Create own functions

The power of CFT is defining own functions. This can be done 
interactively at first, later you may select using an editor. 

Type the following, and press Enter.

```
$ Dir.allFiles(Glob("*.java"))
```

This produces a list of all java files directly and indirectly under the current directory,

After the list of files has displayed, we name this code line, creating a function:

```
$ /JavaFiles
```

Every time we now type JavaFiles and press Enter, we call the JavaFiles function and get a list of Java files
available from the current directory. Since JavaFiles takes no parameters, the ()'s are optional.

To create functions that take parameters, read the doc.



## Save and load scripts

```
$ :save MyScript

$ :load MyScript
```

## Start a new script

To create a new empty script, just type

```
$ :new
```



# More documentation

[Full documentation](doc/Doc.md).

Also check out the example scripts under "code.examples".


# Goals

- Interactive programmable shell
- Compact programming language
- Automation tool



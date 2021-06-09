
# CFT - ConfigTool

## Converted to Maven

*2020-06-09 v2.5.5: Now using Maven instead of ANT*

```
git clone https://github.com/rfo909/CFT.git
cd CFT
mvn package
./cft
```

## Everybody loves Fibonacci ...

```
# Fibonacci
# --
	P(1,List(0,1)) => list
	P(2,20) => iter
	Lib.Data.for(0,iter,1)->i
		list.add(list.last(2).sum)
	|
	list
/fib
```

## What is CFT?

*An interpreted and dynamically typed programming language for automation of daily tasks, for software developers and systems managers.*

CFT started life as a way to create config files, copy files to remote hosts and starting and stopping remote services
via PowerShell. Soon followed collecting log files from multiple sources, and searching through those.

CFT runs as a terminal based REPL, with some shell-like functionality. 

It is backed by a custom functional programming language. Functions are composed interactively,
or more commonly, by editing script files, and is intended for automation at all levels:

- copying files
- running programs on remote hosts
- doing stuff with PowerShell without typing all that text over and over
- setting up software
- collecting log files
- manage services

CFT is written from scratch in Java, implementing a tokenizer and a custom recursive-descent parser,
that creates a tree structure, which is then executed.

## Features


- Compact syntax
- Processing files and directories
- Rich data structures with lists and dictionaries
- Powerful filtering of data
- Running external programs in foreground or background
- User input
- Templating functions for merging data into text
- No global variables - fewer side effects
- Shortcuts for calling code in any script
- Two-tiered error handling system (hard and soft)
- Integrated help system for interactive info on all functions
- All values are objects, with member functions
- Organize sets of functions into scripts (save-files)
- Call functions across scripts
- Use as a desktop calculator
- Lambdas and closures for advanced library functionality
- Supports threads for time consuming operations, such as software installs or updates
- Many library and example scripts
- Up to date documentation with examples
- Youtube tutorial playlist


# Introduction

*In CFT, the function name follows the code.*

Below, the line /Greet creates function Greet from above lines.

```
# Greet users
# --
	List("Tom","Dick","Harry") -> name
		println("Hello " + name)
/Greet
```

CFT is a functional object oriented language. It is primarily used for:
 
- manipulating files and directories
- running external programs in the foreground or background. 

It lets you create custom functions that call global functions, as well as member functions inside objects. All values
are objects, and descriptions of all predefined functions are available via the interactive "help" command. 

## Automation

Useful for all levels of automation:

- searching groups of files
- deploying software with dependencies (ssh / scp or PowerShell)
- start/stop services on sets of servers (ssh or PowerShell)
- collect logfiles, unzipping zipped files, for searching
- automate PowerShell commands - saves a lot of typing
- automate git checkin, checkout, ... or virsh to manage KVM

Communication with remote hosts is done by running external programs, typically SSH, SCP and PowerShell 
in daughter processes (foreground or background). 

## Templating

*Parameters are referred via P(N,defaultValue?) with 1-based position N*

```
# Hello something
# --
	P(1,"world") => something
	Dict.set("something",something).mergeCodes => data
<<< EOF
Hello ${something}.
>>> EOF
	->line
		out(line.merge(data))
/PolitenessCostsNothing
```

CFT supports powerful templating, for creating custom configuration files. 

It also has internal access
to the same tokenizer which tokenizes the CFT language, and has a JSON parser which is written
in CFT itself, returning a CFT data structure, as well as creating JSON text from a CFT data structure.


## Youtube tutorial

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)

CFT has been developed since the spring of 2018, and been on Github since June 2020. 


# Download and compile

Written in Java and built with Maven, which results in a single JAR file. 

Tested on both Linux and Windows. Has no dependencies outside of the standard Java libraries.

# Running

```
./cft

$ 2+2*3
 <int>
 8

$ 4 help
  (lists all functions for integer objects)
  
$ 23.bin
  <String>
  00010111 
  
$ 23.hex
  <String>
  17
$ /f  # name last line as function 'f'
  
$ f.parseInt(16)
  <int>
  23
```

# Creating scripts

*CFT scripts are text files named savefileXxx.txt*

```
$ :new
$ :save MyScript
$ @e
```

The '@e' shortcut opens the script file in a text editor. Works well with both nano and micro, for pure terminal use, but
also supports graphical editors. 

This SearchSourceFiles function identifies all files of multiple types under a directory and its sub-directories, and searches
for free text, then presents the result as columns, with file name, line number and matching line.

```
# General search function for source files.
# --
	P(1,Dir)=>dir
	P(2,List("java","txt"))=>types
	P(3,Input("Search term").get) => st

	# Create Grep object
	grep=Grep(st)  
	
	# Iterate over all files
	dir.allFiles->f 
		type = f.name.afterLast(".")
		
		# Filter on file type
		assert(types.contains(type))
		
		# Call file() function in Grep object
		grep.file(f)->line
			report(line.file.name, line.lineNumber, line)
/SearchSourceFiles
```
Some notes on how this function works:

## Function names

In CFT the code comes first, then the function is named by a separate line with "/" followed by the name. This is a
heritage from when most code was entered interactively, where one would first enter some code, see it ran ok, then
assign a name.

## Function parameters

The P(N,defaultExpr?) calls get the parameter values to the function by position (1-based), and if missing (or null), and there
is a defaultExpr, then resolves it, to get a value for the parameter. Often the defaultExpr will be asking for input.

In the example function *SearchSourceFiles* above, parameter 1 is expected to be the directory where we search for files. If missing, use
current directory. Then we expect a list of file types, by default "java" and "txt", and finally a search term. 

If no search term is given, the code asks for it.

This means functions can be called from other code with parameters, or when parameters are missing, typically 
when called interactively, they can supply sensible defaults, or even ask for input from the user.

## Local variables

In CFT local variables (inside functions) are assigned in two ways:
```
a=1
1=>a
```
## List iteration

In the example below, we call dir.allFiles, which produces a list. We then iterate over it
using the simple arrow "-> loopVariable". The loop body by default continues to the end of the
function.



# Loading and using scripts

```
$ :load MyScript
$ ?
+-----------------------------------------------------
| SearchSourceFiles: # General search function for source files.
+-----------------------------------------------------
| .                : SearchSourceFiles
+-----------------------------------------------------

$ SearchSourceFiles
(?) Search term
...
```

Typing the name of the function in the CFT shell runs it.


# Interactive help

```
$ help
	(lists all global functions)
	
$ Dir help
$ File("x") help
$ List help
$ Dict help
$ "" help
$ 1 help
```


[Full documentation](doc/Doc.md).

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)



# Compact syntax - no custom classes

*Powerful system objects*

The initial goal was a system that was programmed interactively, one line at a time, which required a compact
syntax, to get actual useful work out of a single line of code.

From this also followed the initial decision not to allow custom classes. Classes open a whole can of worms
regarding scope rules, inheritance etc, as well as state management, as "stateless classes" is almost a
contradiction. 

CFT development has focused on creating useful system objects, which the user manipulates via the strictly
functional interface. 


# References

[Full documentation](doc/Doc.md).

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)


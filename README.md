
# CFT - ConfigTool

CFT started life as a way to configuring, deploying, starting and stopping services on remote Windows hosts via PowerShell,
but it is really about automation of all kinds.

- Programmable shell for Linux and Windows
- Terminal based
- Written in Java
- Automation


## Features


- Compact syntax
- Supports threading for parallel operations
- Runs external programs in foreground or background
- No global variables, means less unexpected side effects
- Shortcuts for running code, such as @e to open current script in editor
- Good error reporting
- Two-tiered error handling system (hard and soft)
- Integrated help system for info on all functions, global and inside objects
- Various library and example scripts
- Up to date documentation with examples (doc/Doc.html)
- Youtube tutorial playlist


# Introduction

CFT is a functional object oriented language, which is primarily used for manipulating files and directories, and running
external programs in the foreground or background. 

It lets you create custom functions that call global functions, as well as member functions inside objects. All values
are objects, and descriptions of all predefined functions are available via the "help" functionality. 

## Automation
Useful for all levels of automation:

- searching groups of files
- deploying software with dependencies (ssh / scp)
- automate PowerShell commands both local and remote - saves a lot of
- automate running git and all other command-line programs (such as virsh for KVM)

Communication with remote hosts is done by running external programs, typically SSH, SCP and PowerShell 
in daughter processes (foreground or background). 

## Templating

CFT supports powerful templating, for creating custom configuration files. It also has internal access
to the same tokenizer which tokenizes the CFT language. The JSON parser is written
in CFT itself.

## Internal (thread-safe) data storage

For situations where state is required, either in session or between sessions, or when sharing data with
parallel worker threads, a primitive database is included in CFT. It is thread-safe, and provides a
means for sharing values in a thread safe way, via the "synthesis" functionality, which converts
values to code strings, then using eval() to run the code and recreate (a copy of) the original value.

## Youtube tutorial

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)

Developed since spring 2018. On Github since June 2020. In daily use at work and at home.


# Download and compile

Written in Java and built using Apache ANT, which results in a single JAR file. 

Tested on both Linux and Windows. Has no dependencies outside of the standard Java libraries.

# Running

```
./cft

$ 2+2
 <int>
 4

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

The P(N [,defaultExpr]) calls get the parameter values to the function by position (1-based), and if missing (or null), and there
is a defaultExpr, then resolves it, to get a value for the parameter.

In the example below, parameter 1 is expected to be the directory where we search for files. If missing, use
current directory. Then we expect a list of file types, by default "java" and "txt", and finally a search term. 

If none given, the code asks for it.

This means functions can be called from other code to run with parameters, or when parameters are missing, typically 
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

# Testbed for language mechanisms

CFT also is a bit of a testbed for new and unusual mechanisms.

## Everything is code

There is the strong adherence to code over values,
which resulted in the "synthesis" functionality. 

This converts values, such as a string, a dictionary, or a list of files, 
to code, which can then be stored on file, to be loaded and eval'ed later, or added as a custom function. When
the code is run, it produces the original data, but avoids all concurrency issues by returning new objects every time.

## Compact syntax - No custom classes

The initial goal was a system that was programmed interactively, one line at a time, which required a compact
syntax, to get actual useful work out of a single line of code.

From this also followed the initial decision not to allow custom classes. Classes open a whole can of worms
regarding scope rules, inheritance etc, as well as state management.

CFT has instead been focused on creating useful system objects. 

## List iteration / filtering

Also, iteration over lists is perhaps a bit "strange", using the "-> loopVariable ..." notation, and with the focus
on filtering data, by suppressing values, passing on values or create new values based on old values, or any combination.

## Inner blocks

In addition to normal blocks in curly braces, CFT has a second type of code block prefixed by the word Inner. The
need follows from how list iteration usually means filtering, and how we want to do sub-filtering-jobs inline in
the code of a function, without having to create a helper function.

## Lambdas and Closures

These concepts are well known, and came about as a consequence of being easy to implement, 
but have proven useful for different tasks, particularly creating powerful library code.


[Full documentation](doc/Doc.md).

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)


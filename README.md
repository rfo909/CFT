
# CFT - ConfigTool

CFT started life as a way to create config files, copy files to remote hosts and starting and stopping remote services,
all via PowerShell. Soon followed log collection and searching.

- Terminal based, interactive language, for automation
- Written in Java
- Runs on Windows and Linux


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
- Lambdas and closures for advanced library functionality
- Supports multithreading for time consuming operations
- Many library and example scripts
- Up to date documentation with examples
- Youtube tutorial playlist


# Introduction

CFT is a functional object oriented language. It is primarily used for:
 
- manipulating files and directories
- running external programs in the foreground or background. 

It lets you create custom functions that call global functions, as well as member functions inside objects. All values
are objects, and descriptions of all predefined functions are available via the "help" functionality. 

## Automation
Useful for all levels of automation:

- searching groups of files
- deploying software with dependencies (ssh / scp)
- start/stop services on sets of servers (PowerShell or SSH)
- collect logfiles, unzipping zipped files, for searching
- automate PowerShell commands - saves a lot of typing
- automate git checkin, checkout, ... or virsh to manage KVM

Communication with remote hosts is done by running external programs, typically SSH, SCP and PowerShell 
in daughter processes (foreground or background). 

## Templating

CFT supports powerful templating, for creating custom configuration files. It also has internal access
to the same tokenizer which tokenizes the CFT language, and the JSON parser is written
in CFT itself, returning a CFT data structure


## Internal (thread-safe) data storage

CFT has no global variables, only functions.

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

# Some oddities

CFT is kind of a Domain Specific Language, and the initial concept was that of entering code interactively,
one line at a time. This means the syntax needed to be compact. 

Over time, editing script files in editors became the norm. The shortcut '@e' is frequently used, as it
opens current script in your favourite editor. The interactive terminal window is useful not just for
running your functions, but also for looking up functions inside all types of objects, and inspecting
library scripts.

Below are some of the peculiarities of CFT.

## Everything is code

There is a strong adherence to code over values, which means there are no global variables, only functions.

This resulted in the "synthesis" mechanism, which 
converts values, such as a string, a dictionary, or a list of files, 
to code, which can then be stored on file, to be loaded and eval'ed later, or added as a custom function. 

When synthesized code is run, it produces the original data, but avoids all concurrency issues 
by returning new objects every time.

## Compact syntax - no custom classes

The initial goal was a system that was programmed interactively, one line at a time, which required a compact
syntax, to get actual useful work out of a single line of code.

From this also followed the initial decision not to allow custom classes. Classes open a whole can of worms
regarding scope rules, inheritance etc, as well as state management, as "stateless classes" is almost a
contradiction. 

CFT development has focused on creating useful system objects, which the user manipulates via the strictly
functional interface. 

## List iteration / filtering

Iteration over lists has perhaps a bit "strange" notation, using the "-> loopVariable" notation. The integrated
support for filtering, as well as conversions, via the out() statement, was an early requirement, since these
are very common operations, and the implicit result list made syntax compact.

## Function parameters 

Functions identify parameters with the P(N) expression, where N is the 1-based position of the parameter. But
the P() expression also allows a second part, which is an expression that is a default value. 

The key here is that this second expression is only resolved (run) when it is needed, so it can interactively
ask the user for input.

This means such functions can be called from other functions, with parameters, or they can be called
interactively without parameters, and then become interactive. 


## Loop spaces

With the goal of simple syntax, block notation was considered but rejected at an early stage, when programming
was "one-line-at-a-time". Instead, loop bodies started at the "-> loopVariable" and ended at the end of the
function.

This, however, resulted in many extra functions, and the "PIPE" symbol was born, where the function code is
(at top-level only) split into multiple blocks, so that the output from one, becomes a value on the top of 
the stack for the next, which it can use or ignore, but importantly, the PIPE symbol is an end-marker for all
loops. 

Later, when shifting to using editors instead of entering code interactively, the PIPE could have been
retired, but it remains, as there already was a bit of library code depending on it, and it's kind
of neat, when you know what you're doing.


## Two types of code blocks in curly braces

At the introduction of blocks, what is now called the Lambda, was the first. Then followed the local
block. It's description and implementation varied a bit for a while, until it became clear that we needed
a distinction between what we now call local and Inner blocks.

The concept of having loops populate an implicit result list, meant we needed to differ between the two
types of inline code blocks.
 
The local block inherites the "loop space" from the environment. It can contain inner loops, which are
limited in scope by the block. Calling out() sends data to the the inherited implicit output list.

Inner blocks are isolated
in terms of loop control. Inner blocks can iterate over lists and calculate results, which become result
values in the calling code, without messing with any iteration loop output there.


## Lambdas and Closures

These concepts are well known, and came about as a consequence of being easy to implement, 
but have proven useful for different tasks, particularly creating powerful library code. For example
the Lib:MenuSelect, given a Lambda to extract a label from the list of elements to select from,
lets us directly select any type of object, not just strings. 

If you want to select one from a list of files, create a Lambda that outputs the name of the file.

```
	Lib:MenuSelect(list-of-files, Lambda{P(1).name} )
```


[Full documentation](doc/Doc.md).

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)


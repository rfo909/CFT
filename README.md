

# CFT - ConfigTool

- Programmable shell for Linux and Windows
- Terminal based
- Written in Java
- For automation of all tasks


CFT is a functional object oriented language, which is primarily used for manipulating files and directories, and running
external programs in the foreground or background.

It lets you create functions that call global functions as well as member functions inside objects. All values
are objects, and descriptions of all predefined functions are available via the "help" functionality.

Useful for all levels of automation, from searching groups of files, to deploying software with dependencies.
Communication with remote hosts is done by running external programs, typically SSH, SCP and PowerShell 
in daughter processes (foreground or background). 

CFT supports powerful templating, for creating custom configuration files. It also has internal access
to the same tokenizer which tokenizes the CFT language. The JSON parser is written
in CFT itself.

For situations where state is required, either in session or between sessions, or when sharing data with
parallel worker threads, a primitive database is included in CFT. It is thread safe, and provides a
means for sharing values in a thread safe way, via the "synthesis" functionality, which converts
values to code strings, and using eval() to run the code and obtain (a copy of) the original value.

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
also supports graphical editors. Shortcuts are defined in the CFT.props file. 

Below we create an example function in the MyScript file, but first a few notes ...

## Function names

In CFT the code comes first, then the function is named by a separate line with "/" followed by the name. This is a
heritage from when most code was entered interactively, where one would first enter some code, see it run ok, then
assign a name.

## Function parameters

The P(N [,defaultExpr]) calls get the parameter values to the function by position (1-based), and if missing (or null), and there
is a defaultExpr, then resolves it, to get a value for the parameter.

In the example below, parameter 1 is expected to be the directory where we search for files. If missing, use
current directory. Then we expect a list of file types, by default "java" and "txt", and finally a search term. 
If none given, the code asks for it.

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

## Example function SearchSourceFiles 


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


# Goals

- Automation tool
- Interactive programmable shell
- Extensive and up to date docs
- Interactive help for all objects


[Full documentation](doc/Doc.md).


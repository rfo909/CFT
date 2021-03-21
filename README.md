

# CFT - ConfigTool

- Programmable shell for Linux and Windows
- Terminal based
- Written in Java
- For automation of all tasks


CFT is a functional object oriented language, which is primarily used for manipulating files and directories and running
external programs in the foreground or background.

It lets you create functions that call global functions as well as member functions inside objects. All values
are objects, and descriptions of all predefined functions are available via the "help" functionality.

Useful for all levels of automation, from searching groups of files, to deploying software with dependencies.
Communication with remote hosts is done by running SSH or PowerShell daughter processes.

CFT supports powerful templating, for creating custom configuration files. It also has internal access
to the same parser as that which tokenizes the CFT language. The JSON parser is written
in CFT itself.

For situations where state is required, either in session or between sessions, or sharing data with
parallel threads, a primitive database is integrated in CFT. It is thread safe, and provides a
means of sharing values in a thread safe way, via the "synthesis" functionality, which converts
values to code, using eval() to run the code and obtain (a copy of) the original value.

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)

Developed since spring 2018. On GIT since June 2020. In daily use at work and at home.


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

```
# General search function for source files.
# --
# Defining parameters with default expressions
# - Dir function returns current directory
# - List(...) returns List object
# - Input("...").get asks user for value
# 
# Local variables: 
#   a=1
#   1=>a
#
# List iteration
# List -> ident ...
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

- Interactive programmable shell
- Automation
- Extensive and up to date docs
- Interactive help for all objects


[Full documentation](doc/Doc.md).


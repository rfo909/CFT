

# CFT (ConfigTool)

Programmable shell. Terminal based. Written in Java.

Functional object oriented language. All function docs available interactively.

For all levels of automation, from searching groups of files to deploying software with dependencies.

Powerful templating functions for generating custom configuration files.


[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)


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
  
$ "17".parseInt(16)
  <int>
  23

```

# Creating scripts
```
$ :new
$ :save MyScript
$ @e
```

This opens the script file in a text editor. Works well with both nano and micro, for pure terminal use, but
also supports graphical editors. 

```
# General search function for source files.
# --
# Defining parameters with default expressions
# Dir is current directory
# List() is a list
# Input("...").get asks user for value
# 
# a=1   # local variable
# 1=>a  # local variable alternative notation
# -> ident  # list iteration
# --
	P(1,Dir)=>dir
	P(2,List("java","txt"))=>types
	P(3,Input("Search term").get) => st
		grep=Grep(st)
		dir.allFiles->f 
			type = f.name.afterLast(".")
			assert(types.contains(type))
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
$ Dir help
$ File("x") help
$ List help
$ Dict help
$ "" help
$ 1 help
```


# Goals

- Interactive programmable shell
- Compact programming language
- Automation tool
- Extensive and up to date docs
- Interactive help for all objects


[Full documentation](doc/Doc.md).


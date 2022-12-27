## [Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO).


# Introduction


CFT is an interpreted script language, and an interactive command shell. The aim is to provide a
rich library of functions and objects, to easily automate tasks involving directories and files, 
be it collecting logs, searching source code or creating and deploying templated configuration
files.

There are different ways of running external programs, and display or collect their output, 
even running them in background threads. 

Code can also ask the user for input, as well as present results.



# Two aspects

There are two major aspects to CFT: 

- interactive shell
- programming functions

## Shell-like commands 

The interactive shell offers normal shell-like functionality
for listing files in current directory, navigating the directory tree, copying and deleting, and 
so on. 

## Programming, creating functions

Second, there is programming. All code in CFT is stored as functions, which in turn are organized into "script files",
which we usually work with using an editor. Functions can also be defined interactively.

The input loop of CFT processes both shell-like commands like "ls" and "cd", as well
as the full CFT language, with expressions, loops, function calls.


# Why?

The reason for developing CFT, is mainly the horrors of PowerShell, but also a desire for a an automation
environment and shell that works the same on both Windows and Linux. 

Still, CFT is also inspired by PowerShell, as all values are objects, instead of just strings, like in the
linux/unix shells. 

Lastly it should be mentioned that parsers and intepreters, and language design is a long lasting interest,
ever since creating a preprocessor for Ada for parallel simulation purposes at University pre 1993. 

:-)


# Terminal based - shell-like

The command line interface makes CFT feel like a shell, for navigating the directory tree, and inspecting files,
using the following commands:

- ls
- cd
- cat
- more
- edit
- tail
- mkdir
- rm
- cp
- mv
- touch
- diff

Run global function _Shell for information about the CFT shell-like commands.



## System library

The system library consists of a small set of about 30 global functions. These return values of
various types, like directory or file objects, which in turn contain member functions like getting the
files in a directory, or getting the directory of a file.

The CFT language interpreter supports function calling, local variables inside functions, looping over lists, 
conditionals with "if" and various loop control mechanisms, and some block expressions. And that's about it.

Most functionality is implemented as member functions of objects, such as the Dir object, which is created
via the global "Dir" function, and among others contains a function to run an external program:

```
Dir.run("git","status")
```

So even though there are only some 30 global functions, the system library consists of 500+ functions, spread out across
80+ object types. 

Note that the call to the "Dir" function has *no parantheses*. Those are optional when calling a function
with no parameters.


# Script library

A library of CFT scripts offers various utility functionality. 

In fact, several of the system
commands are implemented using CFT script. An example is the "edit" shell-like command:

```
edit somefile.txt
```

The Java code parses this, then looks up the following definition in the CFT.props file:

```
mEdit = Lambda { P(1)=>file if(file==null, Lib:GetLastResultFile, file) => file Lib:e(file) }

```

(Lambas evolved from something called "macros" initially, hence the config name "mEdit" :-) )

This defines a Lambda function, which is called from the Java code. It takes the single parameter
as P(1), and ends up calling the Lib:e() function with the file, which means that we call
function "e" inside the Lib script.

The script library files are stored primarily under the code.lib directory, with some more
useful examples and utilities under code.examples.

The "@e" shortcut is also defined in CFT.props, and consists of running the following code (not
expressed as a Lambda, since shortcuts do not take parameters):

```
if(Sys.savefile != null, Lib:e(Sys.savefile),"No savefile")
```

It also ends up calling the "Lib:e" function, which handles opening the editor.


In total, CFT has some 17000 lines of CFT scripts under the two code.* directories, among them
a full JSON parser, as well as an XML parser, as the CFT language has access to the Lexer 
used to parse CFT, enabling recursive-descent parsers to be written in CFT.

There are also scripts for automating PowerShell use, installing and working with Docker and
Kubernetes, and many others.

Calls to functions defined in other scripts are always recognized from the syntax.

```
Lib:DirPrivate   ## call function in Lib script
Std.Math.PI      ## call function in Std object
```

```
?Lib:            ## List functions in Lib *script*
:load Lib        ## Load Lib script, making it the current script

Std help         ## List functions inside the Std *object*
```






# Creating a function

```
Dir("/some/path").file("log.txt").append(Date.fmt + " something happened")
```

Pressing Enter, the code line is immediately parsed and executed.

If it works, we may want to give it a name, which is how we create functions.

But before doing that, we will modify the code a bit, into taking the log line as a parameter,
and if no value is given, ask the user to enter it.

When pressing Enter, the code line is again executed, and if it runs okay, we give it the
name "LogLine", by entering a line starting with a slash and the name.

```
P(1,readLine("Enter log line"))=> x Dir("/some/path").file("log.txt").append(Date.fmt + " " + x)
  :
  : 
/LogLine
```

To run the function, we can either just type LogLine and press Enter, which will make it
ask for the value, or we can send it as a parameter.

```
LogLine     # No parantheses needed if no parameters
LogLine("Add this line")
```


# Save and load

Having defined a function, we can decide to save the current script to file, which means
we can load it later to interactively call its functions. Script functions can also be
called from other functions, and also from other scripts, by prefixing the function name with
the name of the script and a colon.

```
:save MyScript
:load MyScript
```

# Current script

The CFT interactive loop has one "current script" at all times, which we can inspect,
using the '?' command, listing the functions in the script. 

```
?
```


# Editing script files

We normally don't enter functions interactively, but instead edit the script file, and
use the interactive shell to call functions, by entering their name and press Enter. 

To edit the current script, assuming it has been saved to a script file, we use what's
called a shortcut. 

```
@e
```

The '@e' shortcut opens current script file in an editor. On windows it uses notepad++ if
installed, otherwise regular notepad. On linux you get to choose between different editors. The
selection is stored for the future.


We can now create an improved and more readable version of our code, since code read from 
cript files allows functions to span more than one line.

The naming of the function follows the interactive syntax, by following the code.

```
# Log directory
# --
  Dir("/some/path")
/LogDir

# Log file
# --
   LogDir.file("log.txt")
/LogFile

# Add log line prefixed with date and time
# --
   P(1,readLine("Log line")) => logLine
   LogFile.append(Date.fmt + " " + logLine)
/LogAdd
```

In addition to calling LogAdd, we can also call the other functions, and even combine them
with shell commands, such as

```
cd (LogDir)
```

Using ()'s around an argument to the shell-like commands, allows us to run CFT expressions.



# Integrated help


All functionality in CFT is documented via the interactive help system.

Global system functions are listed by typing

```
help
```

Here you will see that there exist global functions Dir, Date, List and so on.

Functions inside a system object are listed by putting an instance of some object on the
data stack, followed by "help".

```
Dir help
Date help
List help
""  help
1 help
```

The exampel above also illustrates that String and int are also objects.


## Special help functions

The "help" function only lists functions, either global or inside some object. 

To aid with general syntax, there are two global functions that when you run them, 
display information about statements and expressions in CFT:

```
_Stmt
_Expr
```

A third special help function summarizes the shell-like commands of CFT.

```
_Shell
```



# CFT oddities

- Optional parantheses if no parameters
- No global state
- Foreach notation
- Pipes
- Synthesis - create code from values

## Optional parantheses if no parameters

```
Dir.files

# or

Dir().files()
```

## No global state

CFT is all about functions, and has no global variables. There also is no script state. Scripts are
just collections of functions (name spaces). 

Constant values are easily represented as functions:

```
3.14
/pi
```


## Foreach

Doing a for-each loop is done with the single arrow and an identifier, which is the current value.  

In combination with assert(), reject() and break(), this makes it easy to filter and modify list data. 

The result from a loop is generated with calls to out(), creating a new list.

```
	List(1,2,3,4)->x assert(x%2==0) out(x+100)
	  <List>
	   0: 102
	   1: 104
```
 
## Pipes

In order to do multiple stages of processing, we may use the pipe character ("|") to split the
code of a function into multiple "loop spaces", which simply acts as a terminator of all
loops, and puts the output on the data stack, available via special function "_", or by
directly assigning a variable using stack based variable assignment.

In this example the P() function calls are extended with a default-expression, which is resolved if
the parameter value is null (or missing).

```
	# Count number of files modified within the last week,
	# under current dir and subdirs ("allFiles")
	# --
		P(1,"*.txt") => globPattern
		limit=Date.sub(Date.Duration.days(7)).get  # millis one week ago
		
		Dir.allFiles(globPattern)->f 
			reject(f.lastModified < limit)
			out(f)
		| _.length
	/ModifiedLastWeek
```

## Synthesis

The "synthesis" functionality is a way of serializing data as code. This allows storing big data structures
as strings to file, using the intergrated data object store, Db2.

```
Db2:Set("MyCollection","MyKey",Dir.files)
Db2:Get("MyCollection","MyKey)
	# returns and displays the file list from the call to Set()
```

# Script language or programming language?

CFT is a *programming language* with an interactive command interface.

The reason it should not be considered a script language, is that it does not allow calling external 
programs just by entering their name and parameters, but instead require calls to external programs 
to be written as code:

```
  # If CFT were a scripting language, the following might be a valid
  # line of code in the language.

  git pull origin master

  # However, this is not valid in CFT, as we require a bit of code, such as

  Dir.run("git","pull","origin","master")
  
  # or ...
  
  Dir.run("git pull origin master".split)
```

The disadvantage of having to write code instead of just running a program
is believed to be out-weighed by a richer "vocabulary", as there are 4 different
functions inside the Dir object for running external programs, with varying functionality,
return value and complexity, for example

```
# Run external program and return stdout as List of String
Dir.runCapture("cmd","/c","dir")  
```



# Frequent CFT uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks
- automate powershell command sequences
- built-in JSON and XML parsers (written in CFT)



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

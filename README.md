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




## Programming, creating functions

Second, there is programming. All code in CFT is stored as functions, which in turn are organized into "script files",
which we usually work with using an editor. Functions can also be defined interactively.

The input loop of CFT processes both shell-like commands like "ls" and "cd", as well
as the full CFT language, with expressions, loops, function calls.


# Why a new script language?

The reason for developing CFT, is mainly the horrors of PowerShell, but also a desire for a an automation
environment and shell that works the same on both Windows and Linux. 

Still, CFT is also inspired by PowerShell, as all values are objects, instead of just strings, like in the
linux/unix shells. 

Lastly it should be mentioned that parsers and intepreters, and language design is a long lasting interest,
ever since creating a preprocessor for Ada for parallel simulation purposes at University pre 1993. 

:-)




# System library

The system library consists of a small set of about 30 global functions. These return values of
various types, like directory or file objects, which in turn contain member functions like getting the
files in a directory, or getting the directory of a file.

Show global functions by typing 'help':

```
help
  <obj: <GLOBAL>>
  <GLOBAL>
  # v3.7.4
  # 
  # _Expr() - information about expressions in CFT
  # _Shell() - CFT shell-like commands
  # _Stmt() - information about Statements in CFT
  # 
  # AValue(str,any,metaDict?) - created AValue (annotated value) object
  # Binary(hexString) - convert hex string to Binary value
  # DataFile(file,prefix) - create DataFile object
  # Date(int?) - create Date and time object - uses current time if no parameter
  # Dict(name?) - create Dict object with optional string name
  # Dir(str?) - creates Dir object
  # File(str) - creates File object
  # FileLine(str, lineNo, File) - create FileLine object
  # Float(value,data) - create Float object - for sorting
  # Glob(pattern,ignoreCase?) - creates Glob object for file name matching, such as '*.txt' - ignoreCase defaults to true on win+
  # Grep() or Grep(a,b,...) or Grep(list) - create Grep object
  # Input(label) - create Input object
  # Int(value,data) - create Int object - for sorting
  # List(a,b,c,...,x) - creates list object
  # Regex(str) - creates Regex object
  # Std() - create Std object
  # Str(value,data) - create Str object - for sorting
  # Sys() - create Sys object
  # Term - get terminal config object
  # currentTimeMillis() - return current time as millis
  # error(cond?, msg) - if expr is true or no expr, throw soft error exception
  # eval(str) - execute program line and return result
  # getExprCount() - get number of expressions resolved
  # getType(any) - get value or object type
  # println([str[,...]]?) - print string
  # readLine(prompt?) - read single input line
  # readLines(endmarker) - read input until label on separate line, returns list of strings
  # syn(value) - get value as syntesized string, or exception if it can not be synthesized
```


The CFT language interpreter supports function calling, local variables inside functions, looping over lists, 
conditionals with "if" and various loop control mechanisms, and some block expressions. And that's about it.

## Member functions

Most functionality is implemented as member functions of objects, such as the Dir object, which is created
via the global "Dir" function, and among others contains a function to run an external program:

```
Dir.run("git","status")
```

*Note* when calling a function without any parameters, the ()'s are optional, so "Dir" above calls the global
function, which creates a Dir object (for current directory), and then we call the .run() member function of that object. 

So even with only some 30 global functions, the system library consists of 500+ functions, spread out across
80+ object types. 



# Script library

A library of CFT scripts offers various utility functionality. Much of the "built-in" functionality is ultimately
implemented in CFT, by having the Java code invoke CFT code.

An example of this is the edit command

```
edit somefile.txt
```

The Java code parses the input, then looks up the following definition in the CFT.props file:

```
mEdit = Lambda { P(1)=>file if(file==null, Lib:GetLastResultFile, file) => file Lib:e(file) }

```

This defines a Lambda function, which is called from the Java code. It takes the single parameter
as P(1), which is the file to edit, and ends up calling the Lib:e() function with the file.

The "Lib:e" notation just means function "e" inside script file "Lib". 

The script library files are stored primarily under the *code.lib* directory, with some more
useful examples and utilities under *code.examples*.


## 17k lines of CFT script

In total, CFT has some 17000 lines of CFT scripts under the two code.* directories, among them
a full JSON parser and an XML parser, as the CFT language has access to the Lexer 
used to parse CFT itself, and writing these recursive-descent parsers was easier in CFT than in Java.

There are also scripts for automating PowerShell use, installing and working with Docker and
Kubernetes, and many others.

Calls to functions defined in other scripts are always recognized from the syntax.

```
Lib:DirPrivate   ## call function in Lib script
Std.Math.PI      ## call function in Std *object*
```

```
?Lib:            ## List functions in Lib *script*
:load Lib        ## Load Lib script, making it the current script

Std help         ## List functions inside the Std *object*
```

# Code stats

The CodeStats script contains a function 'main' which filters through the code and
presents a summary:

```
CodeStats:main

Script code:      17344 lines
Java code:        34900 lines
Functions:        507
Shell commands:   18
Object types:     71
Value types:      13
```

To look at the implementation of the CodeStats script, type the following

```
:load CodeStats
?
@e
```

The '?' command lists all functions in current script.

The '@e' is a shortcut to open current script in an editor.

Shortcuts are defined in CFT.props.


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

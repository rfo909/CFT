
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
- create functions

## As interactive shell

When entering commands at the CFT prompt, these fall into one of the *three* following categories:

1. CFT shell-like commands
2. CFT code
3. External program


### (1) "Shell-like" internal commands

The CFT "shell-like" commands, are implemented in CFT using a separate parser from the regular CFT
language parser, which allows shell-like syntax, while being integrated with the CFT language.

So while we could for example run the external command "touch" to create a new file, by implementing
it in CFT, we get a File object back, which we can then do something with, like open in the editor.

The most used "shell-like" commands in CFT are those for navigating the directory tree, such as "ls"
and "cd", which maintains the CFT *current directory*.

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


There exists a global function "_Shell", which lists out up to date information about
all CFT shell-like commands.

```
_Shell
  <boolean>
  true
  # CFT shell-like (interactive) commands
  # -------------------------------------
  #
  #   <TAB> ... - run operating system command or program
  #   shell - run bash or powershell
  #   ls ...
  #   lsd ...
  #   lsf ...
  #   cd <dir>
  #   pwd
  #   cat <file>?
  #   edit <file>?
  #   more <file>?
  #   tail <file>?
  #   touch <file> ...
  #   cp <src> ... <target>
  #   rm <file/dir> ...
  #   mv <src> ... <target>
  #   mkdir <name>
  #   grep <word|str> <file> ... - ex: grep test *.txt
  #   diff <file1> <file2>
  #   showtree <dir>?
  #   hash <file> ...
  #   hex <file>?
  #   which <command>
  #
  # - The 'lsd' lists directories only, and 'lsf' files only.
  #
  # - The 'edit' command opens a file in editor.
  #
  # - Note that the cat/edit/more/tail commands, if given no file argument,
  #   will attempt working with with Sys.lastResult. Example:
  #       touch someNewFile.txt
  #       edit
  #
  # - Note that 'rm' deletes both files and directories, and asks
  #   for confirm when non-empty directories.
  #
  # - The 'cp' command copies both files and directories.
  #
  # - All commands working with files and directories allow both globbing,
  #   local and absolute paths, on both Windows and Linux. To reference
  #   values from Sys.lastResult use :: or :N (get value from list).
  #
  #    ls a*.txt                - globbing
  #    ls /some/path            - absolute path
  #    ls c:\someDir            - absolute path (windows)
  #    ls \\some-server\d$\xxx  - absolute network path (Windows)
  #
  #    cd %someSymbol           - symbol resolving to dir or file
  #    cd (dirExpr)             - dirExpr is a CFT function
  #
  #    cat ::                   - The '::' corresponds to (Sys.lastResult)
  #    cd :N                    - The ':N' corresponds to (Sys.lastResult(N))
  #
  #    <TAB>cp :3 /some/path    - run external program, combining with value :N
  #
  #
  # - Symbols are defined entering %%name which stores lastResult under
  #   that name, usually some Dir or File.
  #
```


### (2) CFT code

Entering CFT code on the command line is *usually to call a function* in the current script, 
but it also allows us to create functions, as at its most basic a function is just a name
for a line of code.

Functions are in turn organized into collections which we call "scripts", and which are saved
as text files, which we can then use regular text editors to develop further.

Below an example of interactively creating a function, although these two lines also 
represent the form which functions are defined in the script files.

```
Date(Dir.newestFile.lastModified)
/LM
```

The first line, when we press Enter, is executed immediately, and returns a Date object for
when the newest file in current directory. Then the second line defines a name for the
previous line, creating the "LM" (last modified) function in current script.

To work with this script file in an editor, we first need to save it, then open in 
an editor. This is done with the two following commands:

```
:save Test
@e
```

The '@e' is a shortcut, which runs CFT library code opening the current script (if saved) in
a text editor. On Linux you will be asked to select which editor, while on Windows it uses
Notepad++ if found, otherwise the ever-present regular Notepad.

Use the '?' command to list functions in current script. 

To call a function in the current script, just enter its name, and optionally parameters,
then press Enter.

```
LM
```

CFT may then display something like this:

```
<obj: Date>
2023-02-02 23:54:22
```




### (3) External programs

The third option, after CFT has decided the input is neither one of the "shell-like" commands
implemented internally, nor CFT code, the command is instead passed to the underlying
shell.

Example:

```
git pull origin master
```

*NOTE:* entering a command line for an external program like this, is
only supported interactively. It is currently *not valid CFT function code*.

To create a function "Pull" to do the same:

```
Dir.run("git","pull","origin","master")
/Pull
```





# Why a new script language?

The reason for developing CFT, is mainly the horrors of PowerShell, but also a desire for a an automation
environment and shell that works the same on both Windows and Linux. 

CFT is still inspired by PowerShell, as all values are objects, instead of just strings, like in the
linux/unix shells. 

Lastly it should be mentioned that parsers and intepreters, and language design is a long lasting interest,
ever since creating a preprocessor for Ada for parallel simulation purposes at University pre 1993. 

:-)




# Built-in functions

CFT consists of a small set of about 30 global functions. These return values of
various types, like directory or file objects, which in turn contain member functions like getting the
files in a directory, or getting the directory of a file.

Show global functions by typing 'help':

```
help
  <obj: <GLOBAL>>
  <GLOBAL>
  # v3.8.3
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
  # Glob(pattern,ignoreCase?) - creates Glob object for file name matching, such as '*.txt' - ignoreCase defa+
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
  # error(cond?, msg) - if cond expr is true or no condition, throw soft error exception
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

So even with only some *30 global* functions, the system library consists of *500+ member functions*, spread out across
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
a full recursive descent JSON parser and also an XML parser.

There are also scripts for automating PowerShell use, installing and working with Docker and
Kubernetes, and many others.

Calls to functions defined in other scripts are always recognized from the syntax.

```
Lib:DirPrivate   ## call function in Lib script 
Std.Math.PI      ## call function in Std object
```

The syntax for getting information about functions also differs:

```
?Lib:            ## List functions in Lib *script*
:load Lib        ## Load Lib script, making it the current script

Std.Math help    ## List functions inside the Std.Math *object*
```

# Code stats

The CodeStats script contains a function 'main' which filters through the code and
presents a summary:

```
CodeStats:main

Script code:      17178 lines
Java code:        35335 lines
Functions:        510
Shell commands:   22
Object types:     71
Value types:      13
```




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

```
"test".length
```



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



# Frequent CFT uses

- daily shell for working with files and directories
- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- generate configuration files on various formats
- install, deployment, restart, cleanup tasks
- automate powershell command sequences


# Download and compile

Written in Java and built with Maven, which results in a single JAR file. 

Tested on both Linux and Windows, through continous use on both platforms. 



```
git clone https://github.com/rfo909/CFT.git
cd CFT
mvn package
./cft

2+3
5
```


[Detailed walkthrough for Windows](INSTALL_WINDOWS.md).


# References

[CFT Introduction](doc/Doc.md).

[Full Reference](doc/Reference.md).

# Appendix

Expressions, statements and shell commands per v4.0.4

```
_Expr
  <boolean>
  true
  #
  # Expressions
  # -----------
  #
  # Logical
  #    bool || bool
  #    bool && bool
  #
  # Compare
  #    >  <  >=  <= == !=
  #
  # Calculate
  #
  #    + - * / % div
  #
  # Assign local variable
  #    ident = Expr
  #
  # Blocks
  #    {...}
  #    Inner{...}
  #    Lambda{...}
  #
  # Exception handling
  #    tryCatch(Expr)
  #    tryCatchSoft(Expr)
  #
  # Various
  #    ( expr )
  #    !expr
  #    -expr
  #    _     # underscore = top value on data stack
  #
  #    if(expr,expr,expr)
  #    if(expr) Stmt [else Stmt]
  #        (note that Expr is also a valid Stmt)
  #
  #    null
  #    ScriptName:func(...)
  #    Sequence(Expr ...)
  #    CondSequence(BoolExpr Expr ...)
  #    SymDict(ident,...)
  #    P(N[,expr])
  #    PDict(Str,...)
  #    SpawnProcess(Dict,expr[,lambda])
  #
  # Type checking
  #    These throw error if failing, mostly for type checking P(N)
  #       expr as String?
  #       expr as int
  #       expr as (List('int','String'))
  #    Using as? instead of as, means return boolean instead
  #       2 as? String   # returns false
  #       2 as String    # error!
  #
  # Background processes
  #    The SpawnProcess() expression has an interactive, simpler syntax:
  #       & expr
  #       & expr , name
  #          The name is a string-expression or an identifier
  #          Use Sys.Jobs or Jobs script to manage (@J* shortcuts)
  #
  # Symbol lookup
  #    %name             # See _Shell for more on symbols
  #
  # Value tokens
  #    int, string, float
  #    true
  #    false
  #
  # Function calls
  #    func
  #    func(...)
  #
  # Dotted lookup
  #    a.b.c(...).d.e
  #
  # Raw string
  #    @ ...
  #


_Stmt
  <boolean>
  true
  #
  # Statements
  # ----------
  #
  # Assign local variable
  #    expr => ident
  #
  # Looping and iteration over lists:
  #    loop ... break(cond)
  #    list -> variable ...
  #
  # Loop control:
  #    assert (boolExpr)
  #    reject (boolExpr)
  #    break (boolExpr?)
  #    continue
  #
  # Loop output:
  #    out (expr)
  #    condOut (boolExpr,expr)
  #    report (expr,expr,...)
  #    reportList (listExpr)
  #
  # The help command shows available functions in objects, and takes two forms:
  #    help           : show global functions
  #    <value> help   : help about top value on stack
  #
  #
  # addDebug (stringExpr)
  # setBreakPoint (stringExpr)
  # timeExpr (expr)
  #
  # Expressions are also statements
  #
  # More system commands are implemented as functions in the Sys object:
  #
  # Sys help


_Shell
  <boolean>
  true
  # CFT shell-like (interactive) commands
  # -------------------------------------
  #
  #   ! ... - run operating system command or program
  #   shell - run bash or powershell
  #   ls ...
  #   lsd ...
  #   lsf ...
  #   cd <dir>
  #   pwd
  #   cat <file>?
  #   edit <file>?
  #   more <file>?
  #   tail <file>?
  #   touch <file> ...
  #   cp <src> ... <target>
  #   rm <file/dir> ...
  #   mv <src> ... <target>
  #   mkdir <name>
  #   grep <word|str> <file> ... - ex: grep test *.txt
  #   diff <file1> <file2>
  #   showtree <dir>?
  #   hash <file> ...
  #   hex <file>?
  #   which <command>
  #
  # - The 'lsd' lists directories only, and 'lsf' files only.
  #
  # - The 'edit' command opens a file in editor.
  #
  # - Note that the cat/edit/more/tail commands, if given no file argument,
  #   will attempt working with with Sys.lastResult. Example:
  #       touch someNewFile.txt
  #       edit
  #
  # - Note that 'rm' deletes both files and directories, and asks
  #   for confirm when non-empty directories.
  #
  # - The 'cp' command copies both files and directories.
  #
  # - All commands working with files and directories allow both globbing,
  #   local and absolute paths, on both Windows and Linux. To reference
  #   values from Sys.lastResult use :: or :N (get value from list).
  #
  #    ls a*.txt                - globbing
  #    ls /some/path            - absolute path
  #    ls c:\someDir            - absolute path (windows)
  #    ls \\some-server\d$\xxx  - absolute network path (Windows)
  #
  #    cd %someSymbol           - symbol resolving to dir or file
  #    cd (dirExpr)             - dirExpr is a CFT function
  #
  #    cat ::                   - The '::' corresponds to (Sys.lastResult)
  #    cd :N                    - The ':N' corresponds to (Sys.lastResult(N))
  #
  #    !cp :3 /some/path
  #
  #
  # - Symbols are defined entering %%name which stores lastResult under
  #   that name, usually some Dir or File.
  #
```

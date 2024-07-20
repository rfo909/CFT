
# Introduction


CFT ("Configtool") is an interpreted script language, and an interactive command shell. The aim is to provide a
rich library of functions and objects, to easily automate tasks involving directories and files, 
be it collecting logs, searching source code or creating and deploying templated configuration
files.

Solving automation issues is done by a combination of programming in the CFT language, and running external
programs. There are different ways of running external programs, and display or collect their output.

Code can ask the user for input, as well as present results.



# Two major aspects

There are two major aspects to CFT: 

- use as interactive shell
- writing code, creating *scripts* (collections of functions)

## As interactive shell

When entering commands at the CFT prompt, these fall into one of the *three* following categories:

1. CFT shell-like commands
2. CFT code
3. External program


### (1) "Shell-like" internal commands

CFT implements support for commands like listing files, navigating directories, copying, moving and
deleting files. plus a few others. The syntax for these commands is similar to corresponding commands in
Linux or Windows CMD, such as:

```
ls
cd ..\somewhere
cat someFile
```

This implementation (in Java) means the commands are the same under Linux and Windows. It also means
that they can return CFT objects, which can be useful:

```
touch someFile.txt
edit
```

The "touch" command creates new file, or updates the lastModified property of an existing file,
and then returns a File object for that file. 

This is utilized by the "edit" command, which when issued without a file name, checks if the 
"last value" is a file, and when it is, opens it in the editor.

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


To get a list of all CFT shell commands, call global function _Shell:


```
_Shell
   # CFT shell-like (interactive) commands
  # -------------------------------------
  # 
  #   <TAB> ... - run operating system command or program
  #   shell - run bash or powershell
  #   ls ...
  #   lsd ...
  #   lsf ...
  #   cd <dir>?
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
  # - & <expr>                  - run expression as background job
  # 
  # - lsd                       - lists directories only
  # - lsf                       - lists files only
  # 
  # - edit                      - open a file in editor
  # 
  # Note that the cat/edit/more/tail commands, if given no file argument,
  # will attempt working with with Sys.lastResult. Example:
  #     touch someNewFile.txt
  #     edit
  # 
  # The 'rm' and 'cp' commands delete/copy both files and directories,
  # and ask for confirm when deleting or copying non-empty directories.
  # 
  # All commands working with files and directories allow both globbing,
  # local and absolute paths, on both Windows and Linux. To reference
  # values from Sys.lastResult use :: or :N (get value from list).
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
  #    <TAB>cp :3 /some/path    - force external program, combining with value :N
  #    <SPACE>...               - force internal command
  # 
  # 
  # - Symbols are defined entering %%name which stores lastResult under
  #   that name, usually some Dir or File. They can be used together with
  #   all shell commands, both those implemented internally and when running
  #   external programs.
  # 
```


### (2) CFT code

Entering CFT code on the command line is usually to *call a function* in the current script,
or in another script, but it also allows us to create functions. Example:

```
Date(Dir.newestFile.lastModified)
/LastModified
```

The first line, when we press Enter, is executed immediately, and returns a Date object for
when the newest file in current directory. Then the second line defines a name for the
previous line, creating the LastModified function in current script.

A CFT script is a collection of functions. By default, CFT starts with an empty script
without a name. When we create a function like LastModified above, it gets added to the script,
although, which still exists in memory only.

We can then save the script to a text file, and in turn open it in an editor, to modify it,
with additional functions.

```
:save Test
@e
```

First we save the script under the name "Test", which results it savefileTest.txt in the current
directory. Then follows "@e", which is a shortcut. It opens the file associated with the current
script in an editor. 

On Linux you will be asked to select which editor, while on Windows it uses
Notepad++ if found, otherwise the ever-present regular Notepad.

Use the '?' command to list functions in current script. 

To call a function in the current script, just enter its name, and optionally parameters,
then press Enter.

```
LastModified
```

CFT may then display something like this:

```
<obj: Date>
2023-02-02 23:54:22
```




### (3) External programs

The third option, after CFT has decided the input is neither one of the "shell-like" commands
implemented internally, nor valid CFT code, the command is instead passed to the underlying
shell.

Example:

```
git pull origin master
```

Now note that using CFT shell commands as well as executing external programs from the
command line of CFT, is NOT valid CFT code syntax. To do the above from program code:

```
Dir.run("git","pull","origin","master")
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
  # v4.2.1
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
  # Glob(pattern,ignoreCase?) - creates Glob object for file name matching, such as '*.txt' +
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
  # print([str[,...]]?) - print string
  # println([str[,...]]?) - print string
  # readLine(prompt?) - read single input line
  # readLines(endmarker) - read input until label on separate line, returns list of strings
  # syn(value) - get value as syntesized string, or exception if it can not be synthesized
```



## Member functions

Most functionality in CFT is implemented as member functions inside objects, such as Dir and File, but also
the basic data types (String, int, float).


*Note* when calling a function without any parameter values, the ()'s are optional. Running the "Dir" function without
parameters, returns a Dir object for the current directory. 

To list all member functions of an object, create an instance of the object, then add "help":

```
Dir help
```

This produces the following list:


```
  # allDirs(Glob?) - returns list of Dir objects under this directory
  # allFiles(Glob?) - returns list of all File objects under this directory
  # allFilesCount(Glob?) - returns number of files matching glob, under this directory
  # cd() - use this Dir as current work dir - returns self
  # copy(File) - copy file to directory, ok if copied ok, otherwise false
  # create() - returns self
  # delete() - return boolean true deleted ok, otherwise false
  # dirs(Glob?) - returns list of Dir objects
  # exists() - returns true or false
  # file(name) - create File object relative to directory
  # files(Glob?) - returns list of File objects
  # filesCount(Glob?) - returns number of files
  # lastModified() - return time of last modification as int
  # name() - returns name (last part)
  # newestFile(Glob?) - return file last modified
  # newestFiles(count,Glob?) - return sorted list (newest first) of newest files
  # oldFiles(seconds) - returns list of File objects older than indicated time
  # path() - returns full path
  # protect(desc?) - set protection status, returns self
  # run(list|...) - execute external program in foreground, waits for it to terminate
  # runCapture(list|...) - execute external program in foreground, but capture output, and return list of stdout lines
  # runDetach(list|...) - execute external program in background
  # runProcess(stdinFile, stdoutFile, stdErrFile, list|...) - start external program - returns Process object
  # showTree(limit?) - returns list of directories where the sum of file sizes > 0 / limit
  # stats() - return dictionary with stats for directory
  # sub(str) - returns Dir object for sub directory
  # unprotect() - unprotect protected directory - error if not protected - returns self
  # verify(str) - verify exists, and return self, or throw soft error with str
```


So even with only some 30 global functions, the system library consists of *500+ member functions*, spread out across
80+ object types.

(Note: these are Java class names, and are included only to give an idea of the types of objects in CFT) 

```
CodeStats:AllObjects

ObjDate
DDWorld
DDLineBrush
DD
DDVector
DDRef
DDBrush
ObjDb2
ObjDb
ObjMath
ObjData
ObjDir
ObjRegex
ObjUtil
ObjLexerToken
ObjLexerNode
ObjLexer
ObjLexerTokenStream
ObjFilter
ObjText
ObjRestApache
ObjStd
ObjInput
ObjJobs
ObjAValue
ObjDict
ObjPlot
ObjSys
ObjColor
ObjCIFS
ObjCIFSFile
ObjCIFSContext
ObjRest
ObjRow
ObjExtProcess
ObjDuration
ObjDataFile
ObjGlob
DDDBezier
DDDTriangle
DDDBrush
DDD
DDDVector
DDDRef
DDDWorld
ObjJavaClass
ObjJavaValueInt
ObjJavaMethod
ObjJavaValueNull
ObjJavaObject
ObjJavaValueObject
ObjJava
ObjJavaConstructor
ObjJavaValueBoolean
ObjJavaValueString
ObjJavaValueLong
ObjLineReader
ObjGrep
ObjFiles
ObjWebServerContext
ObjWebRequest
ObjWeb
ObjWebServer
ObjProcess
ObjFilterReader
ObjEncrypt
ObjDateSort
ObjFile
ObjClosure
ObjGlobal
ObjTerm
ValueFloat
ValueObjFloat
ValueObjStr
ValueObjInt
ValueObjFileLine
ValueString
ValueBoolean
ValueObj
ValueBlock
ValueInt
ValueNull
ValueList
ValueBinary
```


# Script library

CFT comes with a number of scripts, organized in two directories under the CFT home directory:

```
code.lib
code.examples
```

Much of the "built-in" functionality is ultimately implemented in CFT, by having the Java code invoke CFT code.

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


## 21k lines of CFT script

In total, CFT has some 21000 lines of CFT scripts under the two code.* directories, among them
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

Script code:      21165 lines
Java code:        36898 lines
Functions:        549
Shell commands:   22
Object types:     74
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

The exampel above illustrates that String and int are also objects.

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

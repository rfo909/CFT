
# Introduction


CFT ("Configtool") is an interpreted script language, and an interactive command shell. 

It is written 100% in Java, and so can be compiled on both Windows and Linux.

The aim is to provide a
rich library of functions and objects, to easily automate tasks involving directories and files, 
be it collecting logs, searching source code or creating and deploying templated configuration
files.

Solving automation issues is done by a combination of programming in the CFT language, and running external
programs. There are different ways of running external programs, and display or collect their output.


Code can ask the user for input, as well as present results.

# Daily use

CFT is an interactive shell, which is extended with functions collected into *script files*.

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

The "shell-like" commands are implemented in Java, and so are the same under Linux and Windows. They support
globbing as expected, but also return CFT data objects instead of just text. 


```
cd code.lib
ls *.txt
  <List>
   0: savefileAppUI.txt      | 6k  | 6814  | 28d  | 2024-07-19 11:22:35
   1: savefileBangParser.txt | 1k  | 2023  | 28d  | 2024-07-19 11:22:35
   2: savefileConvert.txt    | 1k  | 1092  | 28d  | 2024-07-19 11:22:35
   3: savefileCurses.txt     | 3k  | 3789  | 28d  | 2024-07-19 11:22:35	
       :
```

Each line is automatically prefixed by a 0-based counter, which is the index in the list, in this
case containing File objects. We access elements from the last result using :N, like this

```
more :3
```

This will let us page through the savefileCurses.txt

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


### (2) CFT code

Entering CFT code on the command line is usually to *call a function* in the current script,
or in another script, but it also allows us to create simple functions. Example:

```
Date(Dir.newestFile.lastModified)
/NewestFileDate
```

The first line, when we press Enter, is executed immediately, and returns a Date object for
when the newest file in current directory. Then the second line defines a name for the
previous line, creating the NewestFileDate function in current script.

A CFT script is a collection of functions. By default, CFT starts with an empty script
without a name. When we create a function like NewestFileDate above, it gets added to the script,
although an unsaved script still exists in memory only.

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
NewestFileDate
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

Also note, that one can *force* running an external program from the command line, by prefixing 
it by a TAB. This lets us run the "ls" program of the underlying OS

```
<TAB>ls
```



# Why a new script language?

The reason for developing CFT, is mainly the horrors of PowerShell, but also a desire for a an automation
environment and shell that works the same on both Windows and Linux. 

CFT is inspired by PowerShell, working with objects instead of just strings, as in the linux/unix traditional shells. 

Lastly it should be mentioned that parsers and intepreters, and language design is a long lasting interest,
ever since creating a preprocessor for Ada for parallel simulation purposes at University pre 1993. 

:-)




# Built-in functions / Interactive help

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

When calling a function without any parameter values, the ()'s are optional. Running the "Dir" function without
parameters, returns a Dir object for the current directory, but it can also be invoked with a string, as seen from 
the listing of global functions above:

```
Dir("c:\something")      # windows
Dir("/home/roar/data")   # linux
```



## Member functions

Most functionality in CFT is implemented as member functions inside objects, such as Dir and File, List and Dict, but also
the basic data types (String, int, float).

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
80+ object and value types.

See for example the global objects Std and Sys, which contain various functions, like

```
Sys.environment     
  <obj: Dict>
  Dict [USERDOMAIN_ROAMINGPROFILE OneDriveCommercial WT_SESSION+]
```

The Sys.environment function returns a dictionary object.

```
Util:ShowDict(Sys.environment)
```

This produces a list of all environment values, nicely formatted into columns. To access and use a value, as long
as the value name is a valid identifier, we say:

```
Sys.environment.Path
```



# Script library

In addition to the built-in functions, like Sys and Sys.environment, CFT comes with a library of CFT script files, 
organized in two directories under the CFT home directory:

```
code.lib
code.examples
```

When we invoked Util:ShowDict above, this means run function ShowDict in script Util. The Util script is
located in the code.lib directory.

To look at it, we can locate savefileUtil.txt and open it in an editor.

```
cd code.lib
edit savefileUtil.txt
```

Or we could just load it and then use a shortcut to open current script file in an editor.

```
:load Util
@e
```

We don't need to navigate to the code.lib directory to load the Util script, as the code.lib and code.examples directories
are searched automatically after searching current directory. This is configured in configuration file for CFT.

```
CFT.props
```


## 21k lines of CFT script

In total, CFT has some 21000 lines of CFT scripts under the two code.* directories, among them
a full recursive descent JSON parser and also an XML parser.

There are also scripts for automating PowerShell use, installing and working with Docker and
Kubernetes, and many others. 

Calls to functions defined in other scripts are always recognized from the syntax.

```
Lib:DirPrivate   ## call function in Lib script (colon-separator)
Std.Math.PI      ## call function in Std object (dotted lookup)
```

The syntax for getting information about functions also differs:

```
?Lib:            ## List functions in Lib *script*
:load Lib        ## Load Lib script, making it the current script

Std.Math help    ## List functions inside the Std.Math *object*
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

Strings are objects, and contain a length() function, which we can call like so:

```
"test".length
```



## Special help functions

The "help" function only lists functions, either global or inside some object. 

To aid with general syntax, there are three global functions that when you run them, 
display information about statements and expressions in CFT, as well as the
shell-like commands:

```
_Stmt
_Expr
_Shell
```



# Frequent CFT uses

- daily shell for working with files and directories
- check out + run build + distribute files + cleanup
- manager docker
- search project trees
- collect and search log files 
- generate configuration files on various formats
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


[Install on Windows](INSTALL_WINDOWS.md).

[Install on Ubuntu](INSTALL_LINUX.md).


# References

[CFT Introduction](doc/Doc.md).

[Full Reference](doc/Reference.md).

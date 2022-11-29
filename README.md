## [Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO).


# Automation at all levels

CFT is short for *ConfigTool*, and is an interpreted and interactive programming language
and shell.

It was initiated because of a need for a decent "personal automation tool" in my job as a 
software developer, to run on Windows and Linux. 

It's been in continous daily use since creation in 2018.

Written from scratch in Java; runs both on Linux and Windows environments. 

*README last updated 2022-11-29*


## Terminal based - shell-like

The command line interface makes CFT feel like a shell, for navigating the directory tree, and inspecting files,
using the following commands:

- cd
- ls
- pwd
- cat
- more
- edit


## Functionality

- shell-like command line interface / REPL
- create functions, do interactive testing, use interactive help
- lists and dictionaries
- run external programs in foreground or background
- text templating with merge code processing
- spawn CFT expressions as background threads
- lambdas and closures
- two-tiered exception hierarchy, soft and hard
- integrated data store (Db2)
- simple classes

## Functions and scripts

Automation in CFT is done by creating functions. These are in turn saved in script files. Functions can be one-liners
created interactively, or span multiple lines, created and modified by opening the script file in an editor.

```
Dir.allFiles("*.java").length
/jc
```

Entered interactively, the first line is the code of the function, and as we press Enter, it gets executed. If it does what we want, we
can assign it a name, which is the second line above, creating a function "jc" from the line of code.

Once a function is defined, it can be called by typing its name, optionally followed by parameters inside ()'s. 

```
jc
 <int>
 215
```

Functions are stored in scripts which are simple text files. When starting CFT, you're working with an unnamed
script, which can be saved to file using the following "colon command":

```
:save Test
```

There also is a "colon command" to load a script.

```
:load Test
```

To start creating a new unnamed script, type

```
:new
```

### List functions in current script

CFT has one "current script" at any time. The functions  in the current script can be listed by
entering a '?' and pressing Enter.

```
?

+-----------------------------------------------------
|  jc : Dir.allFiles("*.java").length
+-----------------------------------------------------
| .  : jc
+-----------------------------------------------------
```

### Calling functions

To run the "jc" function from the command line, when the current script is Test, just type "jc" and press Enter. 

If you load another script, and wants to run "jc" inside the "Test" script, type the following, and press Enter

```
Test:jc
```

### Script file = namespace

Script files are collections of functions only; they have no state and no code that is not stored inside
functions. To make a script execute code as it is (re)loaded, just define a function named onLoad

```
println("Welcome")
/onLoad
:save
:load
```

The ":load" reloads the current script, and should result in the Welcome message being displayed. 


### Editing script file

A *shortcut* has been defined to open the current script file in an editor. If the script has not been
saved to a file, you get a short message: "No savefile". 

```
@e
```

We can now enter more complex functions, spanning multiple lines. The last line is the
one starting with '/' which assigns a name to the lines of code before it.

In this example, P(n) means grabbing the n'th parameter (1-based).

```
# Convert IP address to binary format
# --
	ipAddr=P(1) # on format 10.0.0.3
	
	parts = ipAddr.split(".")
	parts->part
		out(part.parseInt.bin)
	| _.concat(".")
/IpToBinary
```

Assuming correct input

```
IpToBinary("10.0.0.3")
 <String>
 00001010.00000000.00000000.00000011
```

### Automatic reload

Whenever we change script files, they get reloaded automatically as we enter the next command in
the CFT input loop. 

*NOTE:* there is no automatic save, so if you add a function from the command line, that you want to keep,
remember to do

```
:save
```



## Colon commands

There are a few commands that operate outside the language, such as saving and loading scripts. These
start with a colon. To list all colon commands, type a colon and press Enter

```
:

Colon commands
--------------
:save [ident]?           - save script
:load [ident]?           - load script
:new                     - create new empty script
:sw [ident]?             - switch between loaded scripts
:delete ident [, ident]* - delete function(s)
:copy ident ident        - copy function
:wrap                    - line wrap on/off
:debug                   - enter or leave debug mode
:syn                     - synthesize last result
:<int>                   - synthesize a row from last result (must be list)
:quit                    - terminate CFT
```

Colon commands are hard-coded into the program.


## Shortcuts

Frequently used functionality, expressed as CFT code, can be stored as shortcuts. These are defined in
the CFT.props file, and by default include:

```
@e       - open current script in editor
@fm      - open file manager for current dir
@cmd     - open OS shell for current dir in new windows
@term    - update terminal size (after resizing window)
@c       - copy selection of files to clipboard, to be copied on @v
@x       - copy selection of files to clipboard, to be moved on @v
@v       - paste selection of files to current dir
@P       - load and set Projects script, for searching
```

To list all shortcuts, type '@' and press Enter.

```
@
```


## Built-in functions

A set of global functions are defined, which return various data, such as a Dir object for the
current directory, or for creating lists or dictionaries. 

To list global functions, type 'help' and press Enter

```
help

  <obj: %GLOBAL%>
  GLOBAL
  # v3.5.2b
  #
  # _Expr() - display information about expressions in CFT
  # _Stmt() - display information about Statements in CFT
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
  # Glob(pattern,ignoreCase?) - creates Glob object for file name matching, such as '*.txt' - igno+
  # Grep() or Grep(a,b,...) or Grep(list) - create Grep object
  # Input(label) - create Input object
  # Int(value,data) - create Int object - for sorting
  # Lib() - create Lib object
  # List(a,b,c,...,x) - creates list object
  # Regex(str) - creates Regex object
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
  # shell() - runs shell as configured in CFT.props
  # syn(value) - get value as syntesized string, or exception if it can not be synthesized
```

The number of global functions is about 30, but the total number of system functions, available as
global functions and member functions inside value objects, is 508 for version 3.5.2b, distributed 
across 85 types of objects.

```
CodeStats:main
```


### Object help

Values in CFT are objects, which in turn contain member functions, like .split()
for String objects, or .bin() for int objects. 

To list member functions inside objects, create an object of that type, followed by 'help'

```
List help
Dir help
File("x") help  ## File() function requires a name, but the file needs not exist
"" help         ## String object help
2 help          ## int object help
Date help
```

### Lists, files and directories

Among the most frequently used global functions are Dir, File and List. Below are some
examples of how these are used. Also note that string values are objects, which have member
functions as well.

```
	## Create empty List object

	List

	## Create List with members

	List(1,2,3)

	## Create Date object for "now"

	Date

	## Get directory object for current dir

	Dir

	## ... or for some explicit path

	Dir("/some/path")

	## Get list of files in current directory

	Dir.files
	
```

### Creating a text file

```
Dir("/tmp").file("theFile").create("this is a test")

# or

File("/tmp/theFile").create("this is a test")
```

The first one creates the File object using the .file() member function of Dir objects, while the second creates
it using the global File() function, and including the complete path as a string.

*NOTE:* using the File() global function without an absolute path, always creates a file in or relative to the
CFT home directory. We usually use Dir.file() to create a file in a certain directory.


### Examine binary files

```
Sys.homeDir.sub("target").file("cft.jar").hex
```


### Some more examples

```
	"test".length
	  <int>
	  4

	23.bin
	  <String>
	  00010111

	"abc".chars.reverse.concat
	  <String>
	  "cba"
	  
	3.14.i
	  <int>
	  3
```


# Objects instead of strings

CFT is inspired by PowerShell, as it works with objects instead of just strings, as in traditional unix shells. This
enables the use of member functions, instead of some huge global namespace.  

Apart from a couple of specialities or quirks, CFT aims at a regular, compact and predictable syntax, 
compared both to PowerShell and bash. 

This means there is no "guessing" as to what the users is trying to do, or silent conversions of data, as in PowerShell. 

Also, differing from both PowerShell and unix shells, there is no automatic substitution of "dollar-expressions", which
means that single and double quotes have no special or different meaning.

```
'"' + "'$x'" + '"'
  <String>
  "'$x'"
```


## Variable substitution / templating

Contrary to PowerShell and bash, CFT performs no automatic substitution of "dollar-expressions" inside
strings, unless told to.

```
	# Create javascript query for MongoDb, counting objects for given status,
	# to be invoked via Mongo shell
	# --
		P(1)=>table   # parameter 1 assigned to local variable 'table'
		P(2)=>status  # ...
		
		# remember printjson() to produce output through Mongo Shell
		Sequence(
			@ printjson(db.<<table>>.find({
			@    status: "<<status>>"
			@ }).count())
		).mergeExpr
	/MongoDbCountStatusJS	
```

The Sequence() expression creates a List, but without the requirement of commas, and the '@ ...' is
the "raw string" format in CFT, which makes a String from the rest of the line. 

The .mergeExpr is a member function of List objects, which by default evaluates expressions inside "<<" and ">>", inserting
resulting values as text into the template sequence. 


# CFT oddities

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


## The Projects script

The "Projects" script was created for quickly searching through project text files (source code, text files, html, css etc).
A shortcut @P loads the "Projects" script. 

Before one can start using it, the config file must be created. First run the Readme function, then call
EditConfig, to enter details for your projects, such as directories and file types.

The most used function is called "S" which means search, but there are other variants, for searching with
multiple arguments etc. 

```
$ @P
CFT <Projects> C:\CFT> Readme
 ...
CFT <Projects> C:\CFT> EditConfig
  # (opens configuration file in editor)

```

Once you have one or more projects defined, you select the project using the "ch" (change) function. The
selection is stored in the integrated database. 

To search through files, run functions "S" (search), "S2" (search with 2 params) and so on.

Show available functions in script with "?".

```
CFT <Projects> C:\CFT> ?

+-----------------------------------------------------
|  License          : # License
|  Readme           : # Readme
|  ConfigFile       : # The projects.txt file
|  EditConfig       : # Edit config file (see Readme)
|  SetFileFilter    : # Limit search to file names containing a certain string
|  ShowFileFilter   : # Show info on current FileFilter (if defined)
|  ClearFileFilter  : # Clear file filter
|  S                : # Search with one parameter
|  S2               : # Search with two parameters
|  S3               : # Search with three parameters
|  SN               : # Search with one positive and one negative parameter
|  gfc              : # Get file content around a given line number as List of lines
|  sfc              : # Show file content around a given line number, report style
|  FL               : # FileLocator, interactive, sorted presentation, newest first
|  TF               : # Show text files (that are being searched)
|  curr             : # Display current project
|  ch               : # Change project
+-----------------------------------------------------
| .                : Curses:Enable
+-----------------------------------------------------
```


# Interactive help

Type "help" lists all global functions. 

```
help
  # v2.9.0
  # 
  # _Expr() - display information about expressions in CFT
  # _Stmt() - display information about Statements in CFT
  # 
  # Binary(hexString) - convert hex string to Binary value
  # DataFile(file,prefix) - create DataFile object
  # Date(int?) - create Date and time object - uses current time if no parameter
  # Dict() - create Dict object
  # Dir(str?) - creates Dir object
  # File(str) - creates File object
     :
     :
```

Note two special functions starting with underscore, which
provide info in built-in statements and expressions when run, like this:

```
_Stmt
  # 
  # Statements in CFT
  # -----------------
  # 
  # Looping and iteration over lists:
  #    loop ... break(cond)
  #    list -> variable ...
  # 
  # Loop control:
  #    assert (boolExpr)
  #    reject (boolExpr)
     :
     :

_Expr
  # 
  # Expressions in CFT
  # ------------------
  # 
  # Logical
  #    bool || bool
  #    bool && bool
  # 
  # Compare
  #    >  <  >=  <= == !=
     :
     :
```


To list functions inside an object, such as string, we type:

```
"" help
  # after(str) - return string following given string
  # afterLast(str) - return string following last position of given string
  # before(str) - return string up to given string
  # beforeLast(str) - return string up to last position of given string
  # between(pre,post) - return string between two given strings
     :
     :
```


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

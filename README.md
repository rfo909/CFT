## [Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO).


# Automation at all levels

CFT is short for *ConfigTool*, and is an interpreted and interactive programming language
and shell.

It was initiated because of a need for a decent automation tool in my job as a 
software developer, that needed to run on Windows and Linux. 

It's been in continous daily use since creation in 2018.

Written from scratch in Java; runs both on Linux and Windows environments. 

*README last updated 2022-11-27*


## Terminal based - shell-like

The command line interface makes CFT feel like a shell, for navigating the directory tree, and inspecting files,
using the following commands.

- cd
- ls
- pwd
- cat
- more
- edit

## Functions and scripts

Automation in CFT is done by creating functions. These are in turn saved in script files. Functions can be one-liners
created interactively, or span multiple lines, created and modified by opening script file in editor.

```
Dir.files("*.java").length
/jc
```

The first line is the code of the function, and as we press Enter, it gets executed. If it does what we want, we
can assign it a name, which is the second line above, naming the function "jc" (java count).

Once a function is defined, it can be called by typing its name, optionally followed by parameters inside ()'s. The
'$' is the prompt.

```
jc
 <int>
 0
```

Functions are stored in scripts which are simple text files. This is done by using the "colon command" to 
save the current set of functions:

```
:save Test
```

There also is a "colon command" to load a script.

```
:load Test
```

Script files are collections of functions only; they have no state and are really only name spaces.

Functions are run from the command line, and they call each other, both inside the same script file
and functions from other script files, using the following syntax:

```
Test:js

```

### Editing script file

A *shortcut* has been defined to open the current script in an editor.

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

Assuming correct input, we get a string value in variable ipAddr. String values offer the split() function, which
returns a list. We then iterate over this
list, and for each part, parse the string to int, then convert it to binary. The foreach loop returns a list of
the values that are passed to the out() statement. The pipe character ("|") marks the end of the loop, and
concatenates the elements of the result from the loop, separating parts by dot, and we get a string value,
such as 

00001010.00000000.00000000.00000011


### Types

CFT is dynamically typed. Variables are not declared, just used, such as the "parts" variable above. The
arrow "->" followed by identifier "part" is the "for-each" of CFT, with "part" as loop variable.

Note that loop variables (the name after the -> arrow) can not be assigned other values inside the loop.

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



## Documentation

The documentation is extensive, and kept up-to-date. 

There also is a Youtube tutorial, plus
another playlist with shorter "howto"-videos.


## Colon commands

There are a few commands that operate outside the language, such as saving and loading scripts. These
start with a colon. To list all colon commands, type a colon and press Enter

```
:
```

Colon commands are hard-coded into the program.


## Shortcuts

Frequently used functionality, expressed as CFT code, can be stored as shortcuts. These are defined in
the CFT.props file, and by default include:

```
@e       - open current script in editor
@fm      - open file manager for current dir
@home    - move to script directory
@c       - copy selection of files to clipboard, to be copied on @v
@x       - copy selection of files to clipboard, to be moved on @v
@v       - paste selection of files to current dir
```

To list all shortcuts, type '@' and press Enter.

```
@
```


## Built-in functions

A number of global functions are defined, which return various data, such as a Dir object for the
current directory, or for creating lists or dictionaries. 

All values in CFT are objects, which in turn contain member functions, like .split()
of String objects, or .bin() of int objects. 

For Dir objects, there are functions that return files
in that directory and so on.

Below we see some examples of global functions.


```
	## Create empty List object

	List

	## Create List with members

	List(1,2,3)

	## Create empty dictionary

	Dict

	## Create Date object for "now"

	Date

	## Get directory object for current dir

	Dir

	## ... or for some path

	Dir("/some/path")

	## Get list of files in current directory

	Dir.files
	
```
  
For function calls with no parameters, the use of ()'s is optional, and usually omitted. The global Dir() function, 
if called without parameters, returns a Dir object for the current directory, but it also supports a String (path) parameter.

Example, creating a file.

```
Dir("/tmp").file("theFile").create("this is a test")

# or

File("/tmp/theFile").create("this is a test")
```

The first one creates the File object using the .file() function of Dir objects, while the second creates
it using the global File() function, and including the complete path as a string.



## Values are objects

All values in CFT are *objects*, and have member functions, which we call to either
modify the object, or get information from it, etc.

There are no primitive types in the classic sense. 

The number values of the "int" type in CFT corresponds to Java long, and the "float" to Java double. 

Strings can be written with single and double quotes. There is no separate type for single characters, those are
just strings as well.


```
	$ "test".length
	  <int>
	  4

	$ 23.bin
	  <String>
	  00010111

	$ "abc".chars.reverse.concat
	  <String>
	  "cba"
	  
	$ 3.14.i
	  <int>
	  3
```

# Using objects instead of just text

CFT is inspired by PowerShell, and works with objects instead of just strings, as in traditional unix shells. 

Apart from a couple of specialities, CFT aims at a regular, compact and predictable syntax, 
compared to PowerShell and bash. 

This means there is no "guessing" as to what the users
is trying to do, or silent conversions of data, as in PowerShell. 

Also, differing from both PowerShell and unix shells, there is no automatic substitution of "dollar-expressions".


The String and List values contain functions for this, which must then be called explicitly. This eliminates
different meaning for single or double quotes.

```
'"' + "$x" + '"'
  <String>
  "$x"
```




## Compact code

Letting values be objects with a rich set of member functions means the code we write can be
quite compact, as much complexity is hidden inside the implementation of those functions (written
in Java). 

For example, to create a hash string for
a file, in order to locate duplicates, or checking if it has changed, we just call the .hash() function of
any File object.

The idea is to let objects, such as File, contain relevant functions that deliver useful results with the
least amount of hassle. The implementation of .hash() has to deal with FileInputStream, and a loop that
reads binary file data into some buffer, to be passed on to the hash function, and finally, code for converting
the binary hash to a hex string.

This means the CFT "API" runs at a *much higher level* than Java, which results in compact code.
 

## Variable substitution / templating

Contrary to PowerShell and bash, CFT performs no automatic substitution of "dollar-expressions" inside
strings, unless told to.

```
	# Create javascript query for MongoDb, counting objects for given status,
	# to be invoked via Mongo shell
	# --
		P(1)=>table
		P(2)=>status
		
		# remember printjson() to produce output through Mongo Shell
		Sequence(
			@ printjson(db.<<table>>.find({
			@    status: "<<status>>"
			@ }).count())
		).mergeExpr
	/MongoDbCountStatusJS	
```

The Sequence() expression creates a List, but without the requirement of commas, and the '@ ...' is
the "raw string" format in CFT. 

The .mergeExpr is a member function of List objects, which evaluates expressions inside "<<" and ">>", inserting
resulting values as text into the template sequence. 


# CFT specialities / oddities

## "Foreach"

One of the pecularities of CFT is its extremely compact notation for doing a "foreach" loop over content,
using a single arrow and an identifier.

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
directly assigning a variable using the stack based assignment operation:

```
3 => x
```


In this example the P(n) get parameter value by position (1-based), while the P(n,defaultExpr) resolve the defaultExpr 
if the parameter is null or missing. 

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


## Simple classes

Since the introduction of lambdas and then closures, which is created by letting a Lambda have a "self" pointer 
to some dictionary, it was possible to create objects with data plus code working on those data. The object
was always created by a function, not some static or global declaration. 

The "class" keyword came much later, and really does the same thing, but in a slightly more compact and readable fashion. Together with
certain refinements in how to access dictionary content, we can now write:

```
	# Greet class
	# --
		P(1) as String => name
		self.name=name
		
		self.greet=Lambda{
			"Dear " + self.name
		}
	/class Greet

	
	# Using it
	# --
		x=Greet("Santa Claus")
		x.greet
		# Returns "Dear Santa Claus"
	/test

```


The Greet function returns an object, with a lambda inside, that we call in the test function.

Notice the special naming of class functions: "/class Greet".

## No global state

CFT is all about functions, and has no global variables. There also is no script state. Scripts are
just collections of functions (name spaces).

State data may of course be stored to external
locations, on file or using the integrated data object store (Db2), but is otherwise not globally
available. This limits
unwanted side effects, which makes script code more robust, and in turn enabled safe multi-threading.



# Scripting vs programming?

CFT is a programming language with an interactive command interface.

It does not readily understand running external programs just by entering their name and parameters, but
instead require calls to external programs to be written as code:

```
  # If CFT were a scripting language, the following might be a valid
  # line of code in the language.

  git pull origin master

  # This is not valid in CFT, as we require a bit of code, such as

  Dir.run("git","pull","origin","master")
  
  # or ...
  
  Dir.run("git pull origin master".split)
```

The disadvantage of having to write code instead of just running a program
is believed to be out-weighed by a richer "vocabulary", as there are 4 different
functions inside the Dir object for running external programs, with varying functionality,
return value and complexity. 



# Frequent CFT uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks
- automate powershell command sequences


## The Projects script

The "Projects" script was created for quickly searching through project files (source code, text files, html, css etc).
A shortcut @P loads the "Projects" script. 

Before one can start using it, the config file must be created. First run the Readme function, then call
EditConfig, to enter details for your projects, such as directories and file types.

The most used function is called "S" which means search, but there are other variants, for searching with
multiple arguments etc. 

```
$ @P
[Projects]$ Readme
 ...
[Projects]$ EditConfig
  # (opens configuration file in editor)

```

Once you have one or more projects defined, you select the project using the "ch" (change) function. The
selection is stored in the integrated database. 

To search through files, run functions "S" (search), "S2" (search with 2 params) and so on.

List functions in script with "?".



## Running background jobs from the command line

Some times we have jobs that take a while, and using the '&' expression, we can delegate those to
run in the background, to avoid blocking the REPL. 

```
  & timeConsumingFunction(...)
```

Strictly speaking, '&' runs an expression, and function calls are expressions.

This spawns off a Process, and registers the job in
a job register, for interactive examination.

The "Jobs" script contains code to manage both running and completed jobs, and in turn is made
available through a few shortcuts, defined in CFT.props.

```
	@J - list background jobs
	@JJ - get result from first completed job (if any)
	@JCL - clear set of completed jobs
	@JFG - bring running job to foreground, for interactivity
```


# Interactive help

Type "help" lists all global functions. 

```
$ help
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
$ _Stmt
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

$ _Expr
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
$ "" help
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

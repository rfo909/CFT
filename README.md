## [Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO).


# Automation at all levels

CFT is short for *ConfigTool*, and is an interpreted and interactive programming language
and shell.

It was initiated because of a need for a decent automation tool in my job as a 
software developer, combined with my interest in parsers and interpreters. 

It's been in continous use since creation in 2018.

Written from scratch in Java; runs both on Linux and Windows environments. 

*README last updated 2022-04-18*


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

Automation in CFT is done by creating functions. These are in turn saved in script files. 

Functions are run from the command line, or they call each other, both inside the same script file
and functions in other script files. Script files have no state, as they are just collections of
functions.

The base of CFT is its built-in objects and functions.

Functions can be created interactively, by assigning a name to the previously executed line
of code, but usually we create functions by editing the script file.

In CFT, code comes before the function name. The P(N) expression returns parameters by position.

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

This example uses the built-in function .split() of the String type, which returns a list. We then iterate over the
list, and for each part, parse the string to int, then convert it to binary. The result is concatenated back to
dotted format, such as 

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
- tryCatch/tryCatchSoft (using two-tiered exception hierarchy, soft and hard)
- integrated data store (Db2)
- simple classes

### Writing recursive-descent parsers in CFT

Given my interest in parsing, CFT has integrated Lexer support, and is sophisticated
enough to write recursive-descent parsers. Two have been created so far:
 
- (2020-11) JSON recursive-descent parser written in CFT
- (2022-04) got XML parser operational, also written in CFT 

The Lib.Text.Lexer uses the same implementation that is used to parse the CFT language, and
builds a graph of nodes matching single characters, for recognizing tokens.

### Editing script code

Originally, the idea was to build code from the bottom up, one line at a time, interactively,
but nowadays we usually edit the script file code in some editor. 

The shortcut @e opens current script file to be edited in notepad or notepad++ on windows, and 
whatever preferred editor is selected on Linux.

Script code is managed via "colon commands", which are management functions outside the language:

```
:save <scriptName>
:load <scriptName>
:new
```


## Documentation

The documentation is extensive, and kept up-to-date. 

There also is a Youtube tutorial, plus
another playlist with shorter "howto"-videos.


## Shortcuts

Frequently used commands or command sequences can be stored as shortcuts. These are defined in
the CFT.props file, and by default include:

```
@e       - open current script in editor
@fm      - open file manager for current dir
@home    - move to script directory
@c       - copy selection of files to clipboard, to be copied on @v
@x       - copy selection of files to clipboard, to be moved on @v
@v       - paste selection of files to current dir
```

List all shortcuts by typing a single '@' and press enter.


## Built-in functions

For v3.4.4 CFT implements 85 object types, among them String, int, List and File. 

There are 507 library functions. Of these, about 30 are global.

The rest are member functions inside the objects, where at
least one exists to create each of the object types, such as 

```
Lib.Text.Lexer   # Lexer object is used to parse text to tokens
```

Data types like lists and strings naturally are the result from lots of library functions.


Below we see some more examples of global functions.


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
  
## Values are objects

All values in CFT are objects, written in Java. These in turn have member functions, which we call to either
modify the value, or get information from it, etc.

There are no primitive types in the classic sense. 

The values of the "int" type in CFT corresponds to Java long, and the "float" to Java double. 

Strings can be written with single and double quotes. There is no value type for single characters, other than String.


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

CFT was inspired by PowerShell, by working with objects instead of just strings, as in bash. 

Apart from a couple of specialities, CFT aims at a regular, compact and predictable syntax, 
compared to PowerShell and bash. Contrary to PowerShell, there is no guessing as to what the users
is trying to do, or silent conversions of data. A list in CFT remains a list until explicitly converted
to something else, etc.

The interactive approach made possible a help system, where one can always run some expression, and list 
member function of the resulting object.

## Compact code

Creating custom functions inside system objects makes CFT code compact. For example, to create a hash string for
a file, in order to locate duplicates, or checking if it has changed, we just call the .hash() function of
any File object.

The idea is to let objects, such as File, contain relevant functions that deliver useful results with the
least amount of hassle. The implementation of .hash() has to deal with FileInputStream, and a loop that
reads binary file data into some buffer, to be passed on to the hash function, and finally, code for converting
the binary hash to hex.

This means the CFT "API" runs at a higher level than Java, and this results in compact code.
 

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
the "raw string" format in CFT. Alternatively we can use inline "here" documents, for easy copy/paste.

The .mergeExpr is a member function of List objects, which evaluates expressions inside "<<" and ">>", inserting
resulting values as text into the template sequence. 


# CFT specialities

## "Foreach"

One of the pecularities of CFT is its extremely compact notation for doing a "foreach" loop over content,
using a single arrow and an identifier.

In combination with assert(), reject() and break(), this makes it easy to filter and modify list data. 

The result from a loop is generated with calls to out(), creating a new list.

```
	$ List(1,2,3,4)->x assert(x%2==0) out(x+100)
	  <List>
	   0: 102
	   1: 104
```
 
## Pipes

In order to do multiple stages of processing, rather than just create helper functions, we may 
split the code of single functions into a sequence of  "code spaces", with the Pipe ("|") character.

The output from one code space is the "piped" as input to the next, via the data stack.


The next code space may then either assign the value to a local variable, with "=> ident" syntax, which
is the stack-based assignment operation, or using the "single underscore" expression, which refers to the 
top value on the data stack.


Example. The P(N) get parameter value by position (1-based) and P(N,defaultExpr) resolve the defaultExpr if the parameter N
is null or missing. Supplying defaults for parameters mean we can run functions without parameters for testing
or common use, with the option of supplying parameters as needed.

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


	# Present files sorted by size, biggest first,
	# in current directory
	# --
		Dir.files->f
			out(Int(f.length,f))
		| _.sort.reverse->x
			out(x.data)
	/BiggestFirst
```

The sort example illustrates all sorting in CFT, which consists of converting a list of something, such as files,
into a list of wrappers that represent a sortable attribute, in this case global function Int(), representing
file size. 

The result list is then sorted, and another iteration unwraps the original elements.

There also exist global functions Str() and Float() following the same pattern. 


## Simple classes

Since the introduction of lambdas and then closures, which is created by letting a Lambda have a "self" pointer 
to some dictionary, it was possible to create objects with data plus code working on those data. The object
was always created by a function, not some static or global declaration. 

The "class" keyword came much later, and really does the same thing, but in a more readable fashion. Together with
certain refinements in how to access dictionary content, we can now write:

```
	# Create simple class
	# --
		class Greet {
			P(1) as String => name
			self.name=name
			
			self.greet=Lambda{
				"Dear " + self.name
			}
		}
	/Greet
	
	# Using it
	# --
		x=Greet("Santa Claus")
		x.greet
		# Returns "Dear Santa Claus"
	/test
```

The Greet function returns an object, with a lambda inside, that we call in the test function.

## No global state

CFT is all about functions, and has no global variables. There also is no script state. Scripts are
just collections of functions, and do not directly contain code.

State data may of course be stored to external
locations, on file or using the integrated data object store (Db2), but is otherwise not globally
available. This limits
unwanted side effects, which makes script code more robust, and in turn enabled safe multi-threading.

This mainly means that all data needed are created, used, then thrown away in order to produce output
from some function, including class objects, which are just a fancy way of managing functionality plus
transitory state, although their power may still also give rise to more complex errors.

The integrated data store (Db2) works by converting data to and from string format, and in doing so
ensures that loading a value from a collection creates an independent copy from the original that
was stored. This avoids race conditions when multiple threads access the same data.



# Scripting vs programming?

  If being a scripting language simply means interpreted, then CFT is a scripting language.

  If "scripting" or script programming instead means running external commands directly 
  from either the command line, or as stand-alone program lines inside scripts, then CFT 
  is a programming language, not a scripting language.

  Example:

```
  # If CFT were a scripting language, the following might be a valid
  # line of code in the language.

  git pull origin master

  # This is not valid in CFT, as we require a bit of code, such as

  Dir.run("git","pull","origin","master")
```

The disadvantage of having to write code instead of just running a program
is believed to be out-weighed by a richer "vocabulary", as there are 4 different
functions inside the Dir object for running external programs, with varying functionality,
return value and complexity. 





# Frequent uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks
- automate powershell command sequences

## The Projects script

For searching through projects (source code, text files, html, css etc), a script called "Projects" has been written
and refined over time. It is easily activated via shortcut @P.

Before one can start using it, the config file must be created. First run the Readme function, then call
EditConfig, to enter details for your projects, such as directories and file types.

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


## Example: ping some hosts

```
	# Readme function
	# --
	<<< EOF
	Check if hosts respond to ping
		(The arrow plus identifier is a foreach)
		(The  /name lines define functions from the preceding code lines)
	>>> EOF
	/Readme

	# Define hosts
	# --
		List("host1","host2","host3")
	/hosts


	# Create report
	# --
		hosts->host report(host,SSH:HostOk(host))
	/checkPing 
```

### Rewrite to do parallel pings

Since pinging hosts that don't respond may take a while, we decide to run
all pings in parallel, then collect information. 

Total time is then the time of the single slowest ping, not the sum of times for all pings.

Threads are created by calling SpawnProcess() with a dictionary for local
variables used in the following expression. This immediately returns Process objects, which
are collected in a list via the out() statement.

When all processes have been spawned, we loop through the processes, wait for
each to terminate, then report its result. 

```
	# Create report
	# --
		hosts->host 
			out(SpawnProcess(SymDict(host), SSH:HostOk(host)))
		| ->proc
			proc.wait
			report(proc.data.host, proc.exitValue)
	/checkPing 
```

Here we first start a set of processes, then iterate over the result from the first loop, which is
now a list of Process objects. We wait for each to complete, and then generate the output via
calls to report().

### The SSH:HostOk function

Above we're calling the HostOk function inside the SSH script. It is implemented as follows.

```
	# Check if server responds on ping
	P(1) =>target
		if(target.contains("@"), target.after("@"), target) =>host
		Lib:run(List("ping","-c","1",host),List,true).exitCode => ex

		ex == 0
	/HostOk
```

It in turn calls function "run()" inside Lib script, which eventually ends up doing a call 
to Dir.runProcess() which actually runs the external program. The details don't matter so much
as the concept of creating a hierarchy of functions with no (or very few) side effects, providing
high level reliability, such as the HostOk function.

## Running background jobs from the command line

Some times we have jobs that take a while, and using the '&' expression, we can delegate those to
run in the background, to avoid blocking the REPL. 

```
  & timeConsumingFunction(...), "Some meaningful name"
```

Strictly speaking, '&' runs an expression, and function calls are expressions.

This spawns off a Process, similar to the SpawnProcess() example above, but in addition registers the job in
a job register. 

The "Jobs" script contains code to manage both running and completed jobs, and in turn is made
available through a few shortcuts, defined in CFT.props.

```
	@J - list background jobs
	@JJ - get result from first of completed jobs (if any)
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

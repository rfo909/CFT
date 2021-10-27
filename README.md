
# Automation at all levels

CFT is short for *ConfigTool*. 

It is an interpreted and interactive shell and programming language. It was initiated
because of a need for a scripting tool in my job as a developer, and from my 
interest in parsers and interpreters.

*README last updated 2021-10-27*

## Terminal based - shell-like

The command line makes CFT feel like a shell, for navigating the directory tree, and inspecting files,
using the following commands.

- cd
- ls
- pwd
- cat
- more
- edit

However, CFT is really about creating and running functions.

## Creating functions

In CFT the code comes before the function name. The P(N) global function returns parameters by position.

```
# Example
# --
	file1=P(1)  # Expects File objects
	file2=P(2)
	
	# boolean return value
	file1.hash==file2.hash 
/FilesMatch
```

*Note* that the two references ".hash" are function calls on the File objects received as parameters.
 
Parantheses are optional when no arguments. 

When we create our own functions, they are organized into script files. They may call each other, both inside the
same script file, and in other script files, as well as member functions inside library objects, such as
the calls to File.hash() above. 

Scripts contain no state, and are just a way of organizing code, making each script essentially a name space. 

## Functionality

- shell-like command line interface / REPL
- create functions, do interactive testing, use interactive help
- lists and dictionaries
- run external programs in foreground or background
- (inline) text templating with merge code processing
- spawn CFT expressions as background threads
- lambdas and closures
- tryCatch with two-tiered exception hierarchy ("soft" and "hard")
- integrated data store (Db2) 
- integrated lexer; JSON recursive-descent parser written in CFT

### Editing script code

Originally, the idea was to build code from the bottom up, one line at a time, interactively,
but nowadays we usually edit script code in some editor. 

The shortcut @e opens current script file to be edited in notepad or notepad++ on windows, and 
whatever preferred editor is selected on Linux.


### Documentation

The documentation is extensive, and kept up-to-date. There also is a Youtube tutorial, plus
another playlist with shorter "howto"-videos.


### Shortcuts

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

### Protection mechanism

CFT has a built-in directory and file *protection*, which may help us avoid modifying critical data on live
systems, such as database files, logs, etc. 

Read more about it in the docs, or view the Youtube tutorial video [episode six](https://www.youtube.com/watch?v=7e-f1gudxpE&list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO&index=7).


### Global functions

CFT currently implements 70+ object types, with 390+ library functions. Of these, about 30 are global, the
rest exist as object member functions, such as the File.hash() invoked in above example.

The global functions return different values, such as the current
directory, an empty dictionary, and so on. 

In the example below, note that the '$' is the prompt.

```
	## Create empty List object

	$ List

	## Create List with members

	$ List(1,2,3)

	## Create empty dictionary

	$ Dict

	## Create Date object 

	$ Date

	## Get directory object for current dir

	$ Dir

	## ... or for some path

	$ Dir("/some/path")

	## Get list of files in current directory

	$ Dir.files
```
  
### Values are objects

All values in CFT are objects, written in Java. These in turn have member functions, which we call to either
modify the value, or get information from it, etc.

There are no primitive types in the classic ("atomic") sense. The "int" type in CFT corresponds to Java long,
and the "float" to Java double. 

Strings can be written with single and double quotes.


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

### Inspired by PowerShell

CFT was inspired by PowerShell, working with objects instead of strings, as in bash. 

Apart from a couple of "peculiarities", CFT strives for a regular, compact and predictable syntax, 
compared to PowerShell and bash. CFT easily calls both, on their corresponding platforms.  

The interactive approach made possible a help system, where one can always run some expression, and list 
member function of the result.

### Variable substitution

Contrary to PowerShell and bash, CFT performs no automatic substitution of "dollar-expressions" inside
strings, unless told to.

### "Foreach"

One of the pecularities of CFT is its extremely compact notation for doing a "foreach" loop over content,
using a single arrow and an identifier.

In combination with assert(), reject() and break(), this makes it easy to filter and modify list data.

```
	$ List(1,2,3,4)->x assert(x%2==0) out(x+100)
	  <List>
	   0: 102
	   1: 104
```
 
### "Pipe"

In order to do multiple stages of processing, rather than just create helper functions, we may 
split the code of single functions into a sequence of  "code spaces", with the Pipe ("|") character.

The output from one code space is the "piped" as input to the next, via the data stack.

The next code space may then either assign the value to a local variable, with "=> ident" syntax, which
is the stack-based assignment operations, or using the "single underscore" expression, which refers to the 
top value on the data stack.

```
	# Calculate total number of lines in text files under current dir and subdirs, for files
	# modified within last hour.
	# --
		Dir.allFiles(Glob("*.txt"))->f 
			reject(f.lastModified < currentTimeMillis-60*60*1000)  # last hour
			out(f.read.length)
		| _.sum
	/TextCountLastHour


	# Present files sorted by size, biggest first
	# --
		Dir.files->f
			out(Int(f.length,f))
		| _.sort.reverse->x
			out(x.data)
	/BiggestFirst
```

The sort example illustrates all sorting in CFT, which consists of converting a list of something, such as files,
into a list of wrappers that represent a sortable attribute, in this case global function Int(). 

The result list is then sorted, and another iteration unwraps the original elements.

There also exist global functions Str() and Float() following the same pattern. 


### No global state

To keep the language simple, CFT *does not support* user defined classes, only user defined functions.

This in turn correlated well with making the language as stateless as possible, for multiple reasons, 
mostly script robustness, but also enabling safe multi-threading. 

CFT has no global variables, and there is no script state. A script in CFT is a collection of related
functions, nothing more. 

There are options for storing data, either to file, or using the integrated Db2 data store. This requires
an explicit effort, not something that just happens, as the goal is to minimize or eliminate unwanted
side effects.


### Values as code

The Db2 data store is able to save most data and values, using a special mechanism called *synthesis*, and
which is the "serialization" format for values in CFT. This produces code from values, which when run, produces
the original values. 

So Db2 stores data as code on string format. When loading from Db2, the code string is run through
eval(), and we get the original data.


# Frequent uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks
- automate powershell command sequences

It's been in daily use since 2019 in my work as a software developer, and is stable. 



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

### Rewritten to do parallel pings

Since pinging hosts that don't respond may take a while, we decide to run
all pings in parallel, then collect information. 

Total time is then the time of the single slowest ping, not the sum of times for all pings.

Threads are created by calling SpawnProcess() with a dictionary for local
variables, and an expression. This immediately returns Process objects, which
are collected in a list via the out() statement.

When all processes have been spawned, we loop through the processes, wait for
each to terminate, then report its result. 

```
	# Create report
	# --
		hosts->host 
			out(SpawnProcess(SymDict(host), SSH:HostOk(host)))
		| ->proc
			println("Waiting for " + proc.data.host)
			proc.wait
			report(proc.data.host, proc.exitValue)
	/checkPing 
```

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

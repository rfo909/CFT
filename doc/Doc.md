
# CFT ("ConfigTool") introduction

Last updated: 2025-03-20 RFO

v4.4.3



# Platform

CFT is written in Java. It has been continously tested on both Linux
and Windows, and easily integrates with running external programs on
both platforms, including PowerShell on Windows.


Development has been going on since May 2018, and on github since version 1.0 in July 2020.



# Shell-like commands


CFT contains a number of "shell-like" commands, with syntax where arguments follow command, separated by space. 

These "shell-like" commands are parsed only when processing input, and so can not be called from function
code, since the syntax is quite different. The shell command parser processes the whole input line, while
the CFT code parser works with tokens, not lines (for the most part).

## List of commands

Run global function to list the shell-like commands that are implemented in CFT (written in Java):

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
  #   grep <word|str> <file|list> ... - ex: grep test *.txt
  #   diff <file1> <file2>
  #   showtree <dir>?
  #   hash <file> ...
  #   hex <file>?
  #   which <command>
  #
  #   & <expr>                  - run expression as background job
  #
  #   lsd                       - lists directories only
  #   lsf                       - lists files only
  #
  #   edit                      - open a file in editor    
    :
```


The syntax for these commands correspond to how they are used in Linux/Unix (where existing), minus flags,
but with support for globbing, relative and absolue paths, on windows and Linux.

```
pwd
cd ..
ls *.txt
cp *.txt ../somePlace
```

## External programs

CFT lets us run external programs from the interactive command line, by just typing and press
Enter. 

```
git status .
Get-Service *tomcat*
```

## The _Shell global function

Run the global function _Shell to get up to date help on the CFT shell command interpreter.


## Paths with space


To access directories of files with space in them, we use quotes, single or double.

```
cd "c:\program files"
```

## Windows backslash


Note that backslash is NOT an escape character in CFT, so just use it like any other character.

## External / Internal forcing ...

The default behaviour when entering a command, is trying to execute a command as an internal function, and if that
fails, trying to run it as an external command. This means that if there is a bug in a
CFT function, often it will produce an error message from the external shell (PowerShell or Bash). 

To solve this, commands can be prefixed with the following:

```
<SPACE>...   # force running as internal command only 
<TAB>...     # force as external command only
```

The latter is useful when we have internal commands with same name as external programs,
for example if we wish to run "ls" in Bash (or PowerShell).

Alternatively we can run the "shell" command, which starts PowerShell or Bash, and then do
whatever we want, followed by "exit" to return to CFT.


## Command history

Display previous commands with typeing single exclamation mark ("!"). 

Get help by type "!?".

```
!?

Command history
---------------

  !            show full history
  !?           help (this text)
  !N           execute command by position in history
  !!           repeat last command
  !xxx         repeat last command starting with xxx
  !xxx*yyy     repeat last command starting with xxx and containing yyy

```


# Prompt colors

The prompt can be configured to use colors. 

First call "Enable" in the Curses script

```
Curses:Enable
```

Then there are two functions in the Prompt script which control colors:

```
Prompt:SetColor
Prompt:SetPathColor
```

## Disabling Curses

In Windows some command windows struggle with ANSI escape codes formatting after running external programs
like git, messing up the display. If this happens, the short term solution is restarting CFT. If it is
too annoying, Curses can be disabled::

```
Curses:Disable
```

# State management

CFT has *no global state*, which means there are no global variables, no script states. This reduces
risk of unwanted side effects. 

Scripts in CFT are only collections of functions, in reality *name spaces*.


# CFT Core types

- String
- int - (Java long)
- float - (Java double)
- boolean
- List
- Dict
- Binary
- Lambda
- Closure

All values in CFT are objects, which may contain functions. Strings can be written using double
or single quotes.

## String literals


Strings are written in 
*single or double quotes*, and can be concatenated with '+', which allows
for all kinds of combinations.

```
"double quotes"
'single quotes'
"'a'"
'a'
'"' + "'a'" + '"'
"'a'"
```

Also, backslash is not used as escape character, simplifying Windows paths.

In script code, there is an additional way of creating strings:

```
@ this is a raw string, extending to the end of the line
```

Since '@' is the shortcut character, you can not enter a raw string directly in the
command line interface, but you can do this:

```
"" + @ something ...
```

## Lists

Apart from String and int, the most used type is the List. A list is created
by calling the global function called List:

```
List(1,2,3)    # creates List object with 1,2,3 as elements
List           # creates empty List object
"abc".chars    # creates List object with "a", "b" and "c" as elements
Dir.files      # List of File objects in current Dir
```

Function calls in CFT don't require parantheses, when no parameters.


# Functions and scripts

CFT lets us define functions. This can be done interactively, but usually we do so
using an editor.

# First function

We start with an interactive example, where first we type the line below.

```
List(1,2,3)
```

This code returns a List object, and displays its' content. Now we assign that code line a name,
as follows:

```
/MyList
```

We have now created a function, called MyList. Typing MyList and pressing Enter, reruns that
line of code. It can also be called from other functions and be part of expressions, such as:

```
MyList.length   # returns 3
```


## Current script

CFT is always has a *current script*. After starting CFT, the current script is empty, and has
no name.

Having defined the MyList function, and perhaps others, we may want to save it, creating a 
script file.

```
:save Test
```

After saving the script, it now has a name.

## Show functions in current script

Entering a single questionmark ("?") at the CFT prompt, shows the content of the current script:

```
?
```

The output looks like this:

```
+-----------------------------------------------------
|  MyList: List(1,2,3)
+-----------------------------------------------------
| .     : MyList
+-----------------------------------------------------
```

## Opening script file in editor

CFT has a concept of *short cuts*. The most frequently used shortcut is the one that opens the
current script in an editor. This requires the current script to be saved to file.

```
@e
```

On Linux you will be asked to select an editor, while on Windows CFT defaults to Notepad, or Notepad++ if found.

Now we can enter functions that span more than one line. 

The first line of a function is displayed when typing '?', and so usually we let that be a comment. The syntax
for defining a function is the same as when working interactively, code first, then
defining the function with a line starting with a slash ("/")


```
# Generate a list
# --
   List(1,2,3)
/MyList
```

## Function parameters and local variables

Functions can take parameters. These are accessed via the global P(N) expression, which takes a 1-based 
position of the parameter. 

Function code allows for local variables, and now we can create an exciting function for adding two
numbers.

```
# Add two numbers
# --
	a=P(1)
	b=P(2)
	a+b
/Sum
```

When editing a script, saving it, there is no need to explicitly load it into the running CFT, as it
discovers when files have been updated, and reload automatically on next command. So now we can 
call this function:

```
Sum(1,2)
```

# Local variable assignment

In addition to traditional assignment, there is a second form, which assigns a value off the stack
to a local variable. This is frequently used when processing function parameters, for readability.

```
a=3                      # traditional form
3=>name                  # alternative form
```


# Looping: iterate over list

Loops in CFT are mostly concerned with iterating over lists. We already have a function that
returns a list, the MyList function from the above section.

Now we can loop over this list, and modify each value. Example:

```
MyList->i out(i*10)
```

This results in a new list with values 10, 20 and 30.

## Counting lines of java code in CFT

Lets us create a new function that returns a list of all Java files under current directory:

```
Dir.allFiles("*.java")
/JavaFiles
```

We now want iterate over the list of files returned, and for each file, read the lines and count them, then
finally, sum up the total. Using the editor to work with current script, we can make it readable as
well:


```
# Sum up number of lines of Java
# --
	JavaFiles->f
		out(f.read.length)
	| 
	_.sum
/JavaLines
```

## For-each

The "single arrow" followed by an identifier is the "for each" construct, with the identifier becoming
the "loop variable".  The out() statement is used to generate output from the loop.

*Note:* loop variables are not regular variables, and can not be reassigned inside the loop.

## The PIPE

The "PIPE" character ("|") terminates all current loops, and delivers the result from these (in this case 
a list of int) to the next part, where the "_" (underscore) function picks it off the stack, then 
calls the sum() function on it, returning a single int value.


## Filtering

Filtering list data is an essential function in CFT. Here is a simple example:

```
List(1,2,3,4,3,2,1)->x if (x%2==0) out(x)
```

This line of code returns a list with the even numbers only: 2,4,2

### assert and reject

```
List(1,2,3,4,3,2,1)->x assert(x%2==0) out(x)
List(1,2,3,4,3,2,1)->x reject(x%2!=0) out(x)
```

These two give the same result as the one where we used "if". 

- assert(boolean) continues with next loop value if boolean condition is false
- reject(boolean) continues with next loop value if condition is true

### break(boolean) 

```
List(1,2,3,4,5,6,7)->x break(x>=4) out(x)
```

The break(boolean) statement aborts the loop when the condition is true, and so the
result of the above is a list with values 1,2,3

### break and continue

The "break" statement can also be invoked without parameter, becoming unconditional.

There also is the "continue" statement, which fetches next loop value, unconditionally. 

These are usually used together with "if".
 


# Calling functions in other scripts

Scripts in CFT are *name spaces*, not programs with state. Indiviual functions inside
a script call each other by using the name, optionally followed by parameters.

To call functions across scripts, we prefix the function call with the name of the
script and a colon, as follows:

```
# Select country
# --
	data=List("Norway","Sweden","Denmark")
	country = Lib:MenuSelect(data)
	println("Selected country: " + country)
/GetCountry
```

Here we use the MenuSelect function which is defined inside the Lib script. 


# Loop output

A loop in CFT generates a List of data that consist of all values for which we
call out() or report().

Examples of out() are shown above, like when filtering data etc. 

## Using report()

The report() statement takes a list of values, separated by comma, and generates a
nicely formatted report for values that print easily, like String, int, Date and
Duration. 

```
Dir.allFiles("*.java")->f
	report(f.name, Date(f.lastModified), f.read.length)
/JavaInfo
```

Running the JavaInfo function produces a list of file names, a nicely formatted date and time for when it was
last modified, and the number of lines.

### Behind the scenes

The report(...) statement is really a glorified out(), which generates a list of single objects
of type Sys.Row

```
report(a, b, c)
	
# is really just short hand for

Out(Sys.Row(a, b, c))
```


### Non-visible report() data

In addition to the values that are displayed easily, and therefore are presented in
the output, the report() statement also lets us carry additional information into the rows,
such as File objects etc, that are not visually shown, but can be accessed with a bit of code.

The idea is that having created a function that
was primarily intended for interactive use, can be extended, so it is usable
for code as well.

Here we will extend the above code to include the File object ("f")

```
Dir.allFiles("*.java")->f
	report(f, f.name, Date(f.lastModified), f.read.length)
/JavaInfo
```

Now, running JavaInfo, the output is the same, but the result contains a hidden column of
File objects. 

To get to one of those File objects, interactively we now may say:

```
JavaInfo
  :
:200.get(0)       ## or just .get, as it defaults to 0
  <obj: File>
  StmtHelp.java 1k 1709 2d18h 2024-07-17 21:14:48
```

### .file and .dir

Picking up files and directories from a report list is so common that instead of remembering or
finding out which column contains the data we're after, the Sys.Row object has some specific
functions

```
JavaInfo
 :
:200.file
  <obj: File>
  StmtHelp.java 1k 1709 2d18h 2024-07-17 21:14:48
```

There is also a .dir function. Note that it does not only look for a Dir object, but if none
is found, if it finds a File object, it returns the directory of that file.

### .date and .duration

These search the row for Date and Duration objects. Even though they are both defined as printable,
we may well want to access them.

### .show - show available columns

All the visible data is available to get(), and to see what is available, be it int or Strings or booleans,
we may need to know the position in the Row where the values of interest are found.

Example

```
JavaInfo
  :
  :
:200.show
  <List>
   0: <obj: File> | 
   1: <String>    | StmtHelp.java
   2: <obj: Date> | 
   3: <int>       | 58
```


### Use in code

Example:

```
JavaInfo->row out(row.get(0).path)
```

This code uses the JavaInfo to grab the File objects, and output a list of full paths.



# Getting help

CFT has a complete system for providing help on all aspects of the language. It falls into
two categories: 

1. getting help about built-in global functions and object functions
2. showing script function code
3. information on internals (statements, expressions and shell-like commands)

## Global functions

CFT has a modest set of global functions. To list these, just enter:

```
help
```

This shows the global functions, around thirthy or so. 

## Object member functions

The majority of functionality in CFT exists as *member functions* inside objects, such as
list objects, strings. There is also the Dir object, representing some directory, the
File object, dictionaries and dates.

The Dir object contains member functions for running external programs. To show these
and other functions inside Dir, enter:

```
Dir help
```

When "help" detects a value on the data stack, it displays the functions inside it.

```
  <obj: Dir>
  doc\
  # allDirs(Glob?) - returns list of Dir objects under this directory
  # allFiles(Glob?) - returns list of all File objects under this directory
  # cd() - use this Dir as current work dir - returns self
  # copy(File) - copy file to directory, ok if copied ok, otherwise false
  # create() - returns self
  # delete() - return boolean true deleted ok, otherwise false
  # dirs(Glob?) - returns list of Dir objects
  # exists() - returns true or false
  # file(name) - create File object relative to directory
  # files(Glob?) - returns list of File objects
  # lastModified() - return time of last modification as int
  # name() - returns name (last part)
  # newestFile(Glob?) - return file last modified
  # newestFiles(count,Glob?) - return sorted list (newest first) of newest files
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

From this list we see four different functions for running external programs. These take either a list, or
a list of parameters, detailing the command.

```
Dir.run("ls","-l")
```

On windows, try

```
Dir.run("cmd","/c","dir")
```

### All values are objects

There are no "primitive" values in CFT. All values are objects with member functions inside. To get
help about these, just do the same as above. Put a value on the stack followed by "help".

```
1 help       # int functions
"" help      # String functions
1.2 help     # float functions
```

From this we see that the "int" objects have a function .bin():

```
13.bin
  <String>
  00001101
```

We also see that both int and float values have two identically named functions which
converts between the two:

```
3.14.i         # returns 3 (.i means as int)
3.f            # converts to float (looks the same)
```

For simplicity, the int objects also have the ".i" and floats have the ".f", so in cases where we 
are not sure if a number is int or float, we can just call the function to make it into what
we want.


## Show script functions

The obvious way of listing the functions inside a script, is to load it, then type '?'.

But we can also list functions inside another script, without loading it, for example
showing the functions defined in the Lib script:

```
?Lib:
```

This gives the usual list of functions. To see the code of a function inside Lib, we
type:

```
?Lib:Confirm
```

To run this function:

```
Lib:Confirm
```

## CFT Internals

The list of global functions, as obtained by typing "help", contains three special
functions:

```
_Stmt        # statements in the language
_Expr        # built-in expressions
_Shell       # all shell-like commands
```



# Dictionaries

One of the fundamental object types, is the dictionary, which is created via global function "Dict". 
This is your basic map.

```
Dict help
```

The Util script has a function to display a dictionary in a readable way. Example:

```
# Some dictionary
# --
	Dict
		.set("a",1)
		.set("b",2)
/MyDict

# Test
# --
	Util:ShowDict(MyDict)
/test
```

## Alternative syntax

When the name of a value in a dictionary, or to be set in a dictionary, is a valid identifier,
and one that does not collide with any of the member functions of the dictionary, we
can use dotted notation for better readability.

```
MyDict.a         # returns 1
MyDict.sum=a+b   # store a+b under name "sum"
```


# Dates

The "Date" global function returns a Date object representing the current date. It can in turn
be modified, by parsing a string, or via a millisecond setting.

```
Date help
```

The Date object parsing and presentation is controlled by a Java SimpleDateFormat pattern.

```
Date.getFormat
```

## Date calculations

The Date object contains a function "Duration", which returns a Duration object.

```
Date.Duration help
```

Example, calculate date and time one week ago:

```
Date.sub(Date.Duration.days(7))
```



# The Sys object

The global function "Sys" returns the Sys object, which contains various system related
functions, such as detecting if running on Windows or Linux, etc

```
Sys help
```

## Environment variables

```
Util:ShowDict(Sys.environment)
```

## CFT start directory

```
Sys.homeDir
```

## Current script file

```
Sys.savefile
```

# The Projects script

This is a library script for searching source code across multiple directories, for multiple file types, and
multiple projects. 

To use, either load the Projects script manually, or use the shortcut

```
@P
```

The first time the Projects script is loaded, it creates an initial projects file, with
a single project, called "CFT Java". It asks to select project, and to do this, enter
either the full name, or part of it.

## Adding projects

All project definitions are found in a single file, which is stored under the "private"
directory under CFT. To add projects, run the following script function:

```
EditConfig
```

This opens the configuration file in an editor. The file contains an extensive comment block
detailing how to add project definitions. 

After saving the configuration file, we can change the current project

```
ch
```

This is the change command. It displays the list of defined projects, and lets us
select one.

The most used function in the Projects script is "S", which searches for a simple string.


## Separate windows

To search different projects at the same time, run CFT in two terminal windows. Each remembers
its own current project. 


# The Investigate script

This is for investigating and taking notes, and is usually invoked with a shortcut

```
@ii
```

It produces a simple loop where one enter text, or paste text. 

It is a very simple script, and the files are stored under the "investigate.d" directory under CFT
home dir.

## @id shortcut

To log data from previous command into the current investigation topic, we use the @id shortcut. If
the previous result is a list, we can choose one element, or the whole list. 

## @ic shortcut

Displays the current investigation log ("investigation cat").



# Reference

[Reference](Reference.md).


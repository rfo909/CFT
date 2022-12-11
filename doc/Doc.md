
# CFT / ConfigTool

Last updated: 2022-12-11 RFO

v3.6.0

# Introduction


CFT is a programming language and an interactive command shell, with focus on data filtering, compactness and
general usefulness. The name is short for "ConfigTool".


CFT implements a system library of 500+ system functions, and some 80+ system object types (v3.5.0). 

There is about 30 global functions, with
the rest existing as member functions inside the system objects.


The system objects represent strings, integers and floats, booleans, lists, dictionaries, files and directories, and many
others related to various uses.


## Compact code


Code in CFT is kept compact, because of powerful library functions.


Example:

```
Dir("/some/path").file("log.txt").append(Date.fmt + " something happened")
```

The corresponding Java code would easily require 10+ lines of code for this single
operation.


We can easily modify this into a callable function
that takes the log line as parameter, or if no parameter given, asks for it:

```
P(1,readLine("Log line"))=> x Dir("/some/path").file("log.txt").append(Date.fmt + " " + x)
/LogLine
```

Entering text *interactively*, each line is considered a function body, and immediately executed.


We then enter "/LogLine" which
creates a named function from the last line of input. We can now run it again by typing LogLine and pressing Enter. It will ask
you to input a "Log line" string, and then add it to the file.


Having defined a function, we can decide to save the current script to file, which means
we can load it later to interactively call its functions. Script functions can also be
called from other functions, in the same or from other scripts.

```
:save MyScript
:load MyScript
```


## Editing script files


We normally don't enter functions interactively, but instead edit the script file. CFT has
the ability to define shortcuts, which by default start with the '@' character. They are defined
in the CFT.props file.


The most frequently used is @e which opens current script in an editor. 

We can now create an improved and more readable
version of our code, as editing the script file, functions can span many lines. 
Function names follow code, just as when entered at the prompt.


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
## Integrated help


All functionality in CFT is documented via the interactive help system.


Global system functions are listed by typing

```
help
```

Functions inside a system object are listed by typing

```
Dir help
Date help
""  help
List help
1 help
```

Here, we get help about the value on top of the stack, showing
member functions inside a Dir object, or a Date object, and so on.


The "Dir" and "Date" identifiers above refer to global functions
that return objects of those types, as documented via simple "help"
without an expression in front.

### Normal expression syntax :-)


Even though there is a data stack, expressions follow
normal "infix" syntax:

```
2+3*5
17

"("+Date.fmt+")"
(2022-04-22 19:20:58)
```




# Platform


CFT is written in Java. It has been continously tested on both Linux
and Windows, and easily integrates with running external programs on
both platforms, including PowerShell on Windows.


Development has been going on since May 2018, and on github since version 1.0 in July 2020.

# Functionality


The CFT programming language is a glue between library objects and functions, user input, and
running external programs. It is designed for data filtering, and file processing,
as well as being interactive.


It is command line based, and can be programmed interactively, creating one-line functions, but
mostly we use editors for creating function code.

## An example

The language is object oriented, with all values being objects. Here we call a
function "bin()" inside an integer object.

```
1.bin
<String>
00000001
```

Parantheses are optional when no parameters to a function.

## Another example

```
Dir.files.length
<int>
12
```


- The "Dir" global function returns the current directory as a Dir-object
- We call the "files" function in the directory object, it returns a list object
- We call the "length" function in the list object, it returns an int object


# Getting help

## Show all global functions

```
help
```

Note the two global functions, _Stmt and _Expr, which produce summaries
of statements and expressions. To run them, just type their name and press Enter:
```
_Stmt
_Expr
```

# Show functions inside value objects


To show all functions inside an object, create an instance of that object followed by the word help.
Specifically, the help statement takes the value on top of the stack and lists it's available
functions.

```
1 help               # integer
3.14 help            # float
"xxx" help           # string
List help
Dict help
Dir help
File("x.txt") help   # the file needs not exist
```
# Create functions


Everything you type in at the prompt is considered code, and executed.


Then, if you want, you can assign a name to the last line of code, and now you have a function.

```
Dir.files.length
<int>
12
/filesInDir
```

Now "filesInDir" is a function, referring to one line of code. It can
be run again as follows:

```
filesInDir
<int>
12
```
# Show your functions


List functions in current script

```
?
```

To show the code of a function:

```
? name
```

If the name doesn't match one function, it is used as a prefix to list a subset
of the functions.


# Save and load - colon commands


Functions are saved into script files, via "colon commands", which are system commands outside
the CFT language.

```
:save myscript
:load otherscript
```

To show all colon commands;

```
:
```

# Edit script file


Instead of entering code via the command line, the script file can easily be opened in
an editor. To do this, the current script must be saved, then enter the following:

```
@e
```

This opens the script in an editor. On linux you will be asked which editor you prefer, and
the selection is persisted for future invocations. To reset the editor selection, type @ee instead.


After changing a script in the editor, and saving, CFT automatically detects the change, and
reloads the script the next time you press ENTER.


The shortcut character can be changed in the CFT.props configuration file.

# Shortcuts and colon commands

Shortcuts are ways of running CFT code, while colon commands are system commands that run completely
outside the language interpreter (written in Java).


View all colon commands:

```
:
```

View all shortcuts:

```
@
```

Shortcuts are defined in the CFT.props file.

# CFT as a shell / the "CFT shell-like commands"


CFT contains a number of "shell-like commands", with different syntax from the regular code, which is 
all about function calls.


- ls | lsf | lsd
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

The syntax for these commands correspond to how they are used in Linux/Unix, with support for
globbing ("*.txt" etc). They are meant for easy navigation around the directory trees, and for
inspecting files, with "cat", "more" and "edit", and so on.
```
pwd
cd ..
ls *.txt
```

The "ls" command comes in two additional versions:

```
lsf   # lists files
lsd   # lists directories
```

Run the global function _Shell to get up to date help on the CFT shell command interpreter.

## Paths with space


To access directories of files with space in them, we use quotes, single or double.

```
cd "c:\program files"

or

cd c:\program" "files
```

## Windows backslash


Note that backslash is NOT an escape character in CFT, so just use it like any other character.

```
Dir("c:\program files\")
/ProgramFilesDir
```


## Combine CFT shell commands with CFT function results

In addition to the above syntax, such as "cd /someDir/xyz" etc, these commands
also support using output from some CFT function. Say we have some functions:

```
Dir("/SomePath/logs")
/LogDir

Dir("/SomewhereElse/xyz").file("data.txt")
/DataFile
```

... then we can do this:

```
cd (LogDir)
edit (DataFile)
```

The expressions should return single Dir or File objects, not lists. If they return a string,
that is used as any other path expression.


## Use "lastResult"

The function Sys.lastResult returns the result from the last interactive command. If that value
is a list, we can get one of the values, by entering the position in the list:

```
lsd 
cd (Sys.lastResult(3))   # usnig ()'s to run CFT expression
```

Since we frequently will need access to "lastResult" when issuing shell-like commands, 
there is a shorthand notation similar to the ':N' colon
command, to obtain a value from the "lastResult" list, or a single colon ':' to return
the "lastResult" as-is. 

```
lsd
cd :3

lsd
:3
cd :

Dir.allFiles("SomeClass.java")
cd :0.dir            # go to directory of file 0 in lastResult

```


## CFT Shell commands not available in code

The CFT shell commands are parsed only when processing input, and so can not be called from function
code.


Function code should instead use the Dir and File functions, etc:

```
dirExpr.cd                 # set current dir
dirExpr.files              # list content in directory
dirExpr.dirs

dirExpr.create
dirExpr.delete

fileExpr.delete

Lib:m(fileExpr)            # The Lib script contains function "m" for paging through a file
Lib:e(fileExpr)            # "e" for opening file in editor
fileExpr.read              # "cat"
fileExpr.touch

```

## Bang commands


CFT supports "bang commands", where one can execute operating system level shell commands.


Bang commands are commands that start or end with "!", and are sent to the shell for execution.

```
!ls -l
ls -l!
```


## Background jobs


CFT lets us run any *expression* (usually a function call) as a background job, using
the '&amp;' expression syntax.

```
& 2+3
& someFunction(...)
```

The "2+3" and "someFunction" are the expressions that are run as background jobs.


To list all jobs, use the @J shortcut. To get the result from the first completed job,
use the @JJ shortcut. There are a few other shortcuts starting with @J that are related to
job management.


Every time a new prompt is to be generated, a check is done to list out any changes to the set
of completed jobs, which means we are alerted with jobs terminate. If we do not care about the
results from any of the terminated jobs, we can use the @JCL shortcut ("jobs clear") which
clears completed jobs from the jobs registry.


Note that jobs don't survive killing the CFT process.


## Symbols 

*v3.5.3*

If we regularly need to go to a particular directory, or check the status of some file,
we can store these as symbols, and use them in expressions or interactively.

```
cd /some/dir
pwd
%%myDir

cd %myDir
%myDir.cd

cat %myDir.files("*.java").first
```
Symbols are persistent and shared between sessions.

To see all symbols use shortcut

```
@%
```

This lists all defined symbols, and gives you the option of deleting symbols.



# The "protect" mechanism


Regularly performing changes, such as copying, deleting and creating files, should be scripted
with code. .

Example:

```
# Our log dir
# We want to process files in this directory, but they MUST NOT
# be modified or deleted, so we add .protect to the Dir()-expression
# --
	Dir("/home/roar/logs").protect
/LogDir
```

Now, if we either interactively or via another function do something like this, then it 
fails with an error:

```
LogDir.files->f f.delete
```

The protect function sets a mark in the Dir or File object to which it is applied. This
mark is in turn inherited by all Dir/File objects derived from it, so LogDir.files
produces a list of files, each with the protection mark set.



# Show content of file


Now if we want to list content of file "TODO.txt", we can enter

```
cat TODO.txt
more TODO.txt
```
## Open a file in editor

```
edit TODO.txt
```


# List basics


Lists are return value from many functions, such as getting the files in a directory.


Lists can also be created
with the global List() function, which takes any number of parameters, and creates a List object
from those values.

```
List                 # empty list
List(1,2,3,4)
Dir.files
"abcdef".chars
"one two three".split
"one:two:three".split(":")
```

Many functions are available on a List object. One frequently used is "nth", which
gets a specific element, defaulting to 0 if no argument, but there also exist "first" and "last"
and a few others.

```
List("a","b","c").first
<String>
a
```

For details of available functions, use the help system:

```
List help
```
# Introduction to loops


Loops in CFT are mostly concerned with iterating over lists. Let's create a list:

```
Dir.allFiles(Glob("*.java"))
```

This line of code generates a list of all java files under the current directory or sub-directories.
When we see that the code works, we give it a name.

```
/JavaFiles
```

We then iterate over the list of files returned, and count the number of lines in each, then
sum it all up, creating the "linecount" function.

```
JavaFiles->f out(f.read.length) | _.sum
<int>
18946
/linecount
```

The "single arrow" followed by an identifier is the "for each" construct, with the identifier becoming
the "loop variable".  The out() statement is used to generate output from the loop.


The "PIPE" character ("|") terminates the loop, and delivers the result from the loop (in this case a list of int) to the next part,
where the "_" (underscore) function picks it off the stack, then calls the sum() function on it,
returning a single int value.


**Note:** loop variables are not regular variables, and can not be reassigned.

## Filtering


Filtering list data is an essential function in CFT. Here is a simple example:

```
List(1,2,3,4,3,2,1)-> x assert(x>2) out(x)
<List>
3
4
3
```

The assert() works like "if condition not satisfied, continue with next value"

# Local variables


Function code may use local variables for simplifying expressions.

```
a=3 b=2 a+b
<int>
5
```

Assignment can also be done "stack based", where the assignment picks the current value
off the stack and stores it into a variable:

```
3=>a 2=>b a+b
5
```

Example:

```
List("java","txt")
/types

Dir.allFiles->f type=f.name.afterLast(".") assert(types.contains(type)) out(f)
/textfiles
```

This function lists all files of type .java and .txt under current directory.

# Files

```
File("x.txt")
<obj: File>
x.txt   DOES-NOT-EXIST
```

The File() function requires a name, and returns a File object. As seen
above, the file needs not exist.


File objects created with a simple file name (no path), are always located in
the CFT home directory. This gives predictability for certain data files etc.


To access or create files in other directories, enter an absolute or relative
path in the parameter to File(), or use the .file() function inside
some Dir object:

```
Dir("/some/path").file("x.txt")
```
## Page through a file


To page through text file

```
more x.txt
```

To do the same with function code:

```
SomeDir.file("x.txt") => file Lib:m(file)
```
## Edit a (text) file


To edit a text file

```
edit x.txt
```

To do the same with function code:

```
SomeDir.file("x.txt") => file Lib:e(file)
```
## Show file as hex


To page through hex listing of file

```
File("x.txt").hex
```
## Encoding


Default encoding is "ISO_8859_1", but this can be changed, for example:

```
File("x.txt").encoding("UTF-8").create(...)
```

## end-of-line


For windows, the default line terminator is CRLF, and for Linux it is LF. This can be
overridden as follows:

```
someFile.setWriteCRLF
someFile.setWriteLF
```

CFT reads both formats, and so to ensure a text file has all lines end with for
example LF, we can do the following:

```
f=File("x.txt") lines=f.read f.setWriteLF.create(lines)

# or more compact
f=File("x.txt") f.setWriteLF.create(f.read)
```

# Directories

```
Dir
<obj: Dir>
ConfigTool/ d:5 f:20
```

Calling the Dir function with no parameters returns a Dir object for the current directory.


The Dir
object offers multiple member functions, one of which is 
**.files()**, which produces a list of files in
the directory. Another is 
**.allFiles()** which return files from all subdirectories as well.


These support "globbing", which is to use "*" to match groups of files.

```
Dir.files("*.txt")
Dir.allDirs(".git")
```
## Create a subdirectory

```
Dir.sub("someDir").create
```
## Parent directory


To get the parent directory of a Dir object:

```
Dir.sub("..")
```
## Get files in a directory

```
Dir.files
```
## Create a file in a directory

```
Dir.file("x.txt").create("something")
```
## Get immediate directories in a directory

```
Dir.dirs
```
## Get all files recursively under a directory

```
Dir.allFiles
```
## Get all directories recursively under a directory:

```
Dir.allDirs
```

Note: these are delivered in a sequence so that if all are empty of files, they can
be safely deleted (leaf-directories first, root last).

## Delete a sub-directory


The sub-directory must be empty

```
Dir.sub("something").delete
```
## Set current directory


Apart from navigating interactively, to set current directory via code:

```
Dir.cd
```
## Newest file in directory

```
Dir.newestFile
Dir.newestFile(Glob("*.log"))
```
# Core types



- String


- int - (Java long)


- float - (Java double)


- boolean


- List


- Dict


- Binary


- Lambda



All values in CFT are objects, which may contain functions. Strings can be written using double
or single quotes.

## String literals


Strings are written in 
**single or double quotes**, and can be concatenated with '+', which allows
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
## Dictionaries


Dictionaries are maps that store any value identified by names (strings).

```
x=Dict x.set("a",1) x.get("a")
1
```

Note: We will often store values by names that are valid identifiers. For these we can use dotted notation
both when setting and fetching the values:

```
x=Dict x.a=1 x.a=x.a+1 x.a  # returns 2
```
### SymDict


A special global expression, SymDict is used to create a dictionary from a list of
symbols (identifiers), which must be present as local variables, or parameterless functions. This
saves some typing. Example code:

```
# Without SymDict()
a=1
b=2
dict=Dict
dict.a=a
dict.b=b

# With SymDict
a=1
b=2
dict=SymDict(a,b)
```

Note that assigning a value to a dict, using dotted notation, returns the Dict object, and
using the underscore function, which refers to the value on the data stack, we can
initialize dictionaries as follows:

```
Dict
_.a=1
_.b=2
=> dict
```
### Dictionary name


Dictionary objects support a simple string name, in addition to named properties.

```
Dict("name")
```

The name can be accessed via Dict.getName() and modified/set via Dict.setName(). To
clear, do Dict.setName(null)


This name property of dictionaries is used to convey type information for dictionaries,
together with "as" type checking. This is discussed in more detail elsewhere. A simple
example:

```
Dict("Point")
_.x=1
_.y=2
=> point

# Some function expecting a Point dictionary as parameter
# --
P(1) as &amp;Point => point
...
/f
```

The '&amp;' in front of the type is to indicate that we are interested in the name of a Dict. Omitting it
we can check for regular types, such as int, String, List, Dict and a whole lot of others.

## Binary type


Used in connection with encryption etc. Can be created from strings, or represent
the content of a file.

```
"password".getBytes("UTF-8")
<Binary>
0x..
```

Check the Lib.Util object, which contains functions that create objects Encrypt and Decrypt.

# List processing


Lists can be created manually using the global List() function.

```
List(1,2,3)
List("a","b","c")
```

A much used way for creating lists of strings, is to use the string function split(), which by default
splits a string on spaces. This means the following produce the same result.

```
List("a","b","c")
"a b c".split
```
## Iterating over list content


The iterator in CFT takes the form of an arrow followed by a loop variable. For a loop construct
to return output, we use the out() statement inside.

```
"1 2 3".split->x out("a"+x)
<List>
0: a1
1: a2
2: a3
```

The result is a list of strings, as displayed.

## Filtering with assert(), reject() and break() + out()


Using the assert() statement, we may abort processing for elements that do not meet a condition.

```
Dir.allFiles->f assert(f.name.endsWith(".java")) out(f)
```

The reject() statement is the inverse of assert(), and aborts processing for elements that meet
a certain condition.

```
List(1,2,3,2,1)->x reject(x>2) out(x)
<List>
1
2
2
1
```

The break() statement terminates ALL LOOPS if the condition is true.

```
List(1,2,3,2,1)->x break(x>2) out(x)
<List>
1
2
```
## The condOut() statement


In addition to controlling loops with assert/reject and break, there is the condOut()
statement, which takes a boolean condition as first parameter, and the value to
be sent out as second parameter. Can be useful some times.

```
List(1,2,3,2,1)->x condOut(x<2,"(") out("b") condOut(x<2,")") | _.concat
<String>
(b)bbb(b)
```

Alternatively one can use "if". Using a multi-line example here:

```
List(1,2,3,2,1)->x
if (x<2) out("(")
out("b")
if (x<2) out(")")
| _.concat
/f
```
## Produce columns with report()


Using report() instead of out() lets us produce as output a list of strings,
where multiple parameters to report() is formatted into columns. Example:

```
Dir.files->f report(f.name, f.length)
```
## List addition


Two lists can be added together with "+".

```
List(1,2) + List(3)
<List>
1
2
3
```

Also, elements can be added to a list with "+" as long as the list comes first.

```
List(1,2)+3
<List>
1
2
3
```
## List subtraction


Using "-", we can remove one or more elements from a list.

### Removing a single value from a list

```
List(1,2,3,2,1)-2
<List>
1
3
1
```
### Removing multiple values from a list

```
List(1,2,3,2,1)-List(2,3)
<List>
1
1
```
# Sorting


The List object has a single .sort() member function, which does the following:



- if all values are int, sort ascending on int value


- if all values are float, sort ascending on float value


- otherwise sort ascending on "string representation" of all values



Now, to sort other types of values, we use a "trick", which consists of wrapping each
value inside a special wrapper object, masking the original values as either int, float
or STring, then sort, and finally extract the actual value from the wrappers.


To sort a list of files on their size, biggest first, we do the following:

```
Dir.files->f
	out(Int(f.lastModified,f))
| _.sort.reverse->x
	out(x.data)
```

The first loop wraps each File object inside an Int object, which is created by
supplying two values to global function Int: the value to sort on, and the object itself.


Then the resulting list is "piped" to code that picks it off the stack, sorts and
reverses it, before iterating over the result, and for each object (now the Int objects),
outputs the original File object, available via the .data() function.

## Int(), Str() and Float()


Similarly there is a global Str() function for sorting on strings, and Float() for
sorting on floats. Together with Int() function, these produce Str, Float and Int objects,
which are actually subclasses of the regular "String", "float" and "int" value types, with
the additional function .data() to retrieve the original value.

## Converting between int and float


Both the "int" and "float" type contain two functions for converting to int and float:

```
2.f     becomes float 2.0
2.i     remains int   2
3.14.f  remains float 3.14
3.14.i  becomes int   3
```
# Savefiles - "scripts"

## Save


To save all named functions, enter the special command below

```
:save Test
```

This creates a file under the CFT home directory,
called savefileTest.txt.

## Load

```
:load Test
```
## Create new empty script


To create a new script from scratch, there is the colon command:

```
:new
```

## The @e shortcut


A common shortcut is @e, which opens current savefile in an editor:

```
@e
```

Shortcuts can be redefined in the CFT.props file.

## CFT.props - codeDirs


The CFT.props file contains the following line by default

```
codeDirs = . ; code.examples ; code.lib
```

The codeDirs field defines a search order when loading scripts (followed by current dir)


The code.examples contains some example code for various use, while code.lib contains
library code, used by most other scripts.


Each script remembers where it was loaded from, so when saving it, it is written back
to that location.

# Comments


The hash character '#' indicates that the rest of the line is a comment.

# Calling functions in other scripts


Sometimes we want to call a useful function in another script file. This is
implemented with with the following syntax:

```
Script:Function (...)

Example:
Lib:Header("This is a test")
```
# Examining external scripts


The '?' interactive command has an extended syntax that allows you to list functions inside
another script, as well as listing the code of particular function.

```
?Lib:                  # lists functions inside Lib
?Lib:m                 # displays code of function 'm' (to page through a text file)
```
# Helper / local functions


In many cases, we need to create helper functions, which should not be visible as part
of the script interface, as seen from other scripts. This is done by defining the function
as follows:

```
23
//SomeConstant
```

The '?' command omits these local functions, for a cleaner summary of the main functions
of a script. To see all functions, type '??'. The local functions are
then prefixed by a single '/' slash.


When inspecting
script from outside via the "?ScriptName:" functionality, the local functions are
also not displayed, while using "??ScriptName:" includes them.


Also note, that "local" functions are not private in the Java sense, and are fulle callable
from the outside. Marking functions as local is all about filtering what is shown on the
single "?" command.

# Displaying all known scripts


The function Lib:Scripts displays all available scripts, sorted by the directories given
in the CFT.props file.


The shortcut @scr calls this function.

# Nested loops


Loops are implemented using the "for each" functionality of "-> var". Loops may well be nested.

```
List(1,2,3)->x List(1,2,3)->y  out(x*y)
<List>
0: 1
1: 2
2: 3
3: 2
4: 4
5: 6
6: 3
7: 6
8: 9
```

In this case, the body of each loop is all code following the "-> var"
construct. But this can be changed using the "pipe" symbol, which "closes" all loops.

# Code spaces - "pipes"


**Code spaces have also been called "loop spaces" in earlier versions of the doc.**

The body of any loop is the rest of the code of the function, or until a "pipe" symbol
is found. The pipe symbol ("|") partitions code into a sequence of
**code spaces**. Loops are limited within single code spaces, so the "pipe"
effectively is an end marker for all current loops.


The way a "pipe" works, is to wait for any current loops to terminate, then take the
return value from that code space and putting it onto the stack for the next loop
space to work with (or do something else). Example:

```
Dir.files->f out(f.length) | =>sizes sizes.sum
```

This single line of code first contains a loop, which outputs a list of integers for
the sizes of all files in the current directory. Then the "pipe" symbol terminates that
code space, and creates a new one, where we pick the result from the previous loop
space off the stack and assigns it to a local variable. We then apply the sum() function to it.


To save us some typing, the special expression "_" (underscore) pops the topmost value off
the stack.

```
Dir.files->f out(f.length) | _.sum
```

As we see from the above code, code spaces don't 
**need** to contain loops. The
following is perfectly legal, although a little silly.

```
2+3 | | | | =>x x | =>y y | _ _ _ |
```

It returns 5.

## Result value from a code space


All function bodies in CFT consist of one or more 
**code spaces**. The return value
from the function is the result from the last code space.

### Code space result value


If a code space contains loop statements, the result value is a list of data generated
via calls to out() or report() statements. If no actual iterations take place, or
filtering with assert(), reject() or break() means no data is generated via out() or report(),
then the result list is empty.

### Otherwise ...


A code space that doesn't contain loop statements, has as its result value the topmost
element on the stack after all code has executed. If there is no value on the stack,
the return value is 
**null**.

# Function parameters


Custom functions can take parameters. This is done using the P() expression, which
identifies the parameter by position. Note that 
**parameter position is 1-based**.

```
P(1)=>a P(2)=>b a+b
```

This is a valid function, but entering it interactively fails, because it is immediately
parsed and executed, and there are no parameter values. To overcome this, the P() expressions
take a second parameter, which is a default value.


The default value parameter to P() is important for several reasons.



- Allows the function code to execute while being developed interactively


- Allows for default values when function is called without parameters, or when called with null-values


- May act as documentation in the source


- Provides an elegant way of making functions interactive and non-interactive at the same time,
as the default expression is evaluated only when parameter is not given (or is null),
and may then ask the user to input the value.



Above example again, now with default values for parameters:

```
P(1,1)=>a P(2,2)=>b a+b
<int>
3
/f
f(5,10)
<int>
15
```
# User input


CFT contains the following for asking the user to enter input:

```
value = Input("Enter value").get
value = readLine("Enter value")
```

The difference is that Input remembers unique input values, and lets the user
press enter to use the last (current) value, or enter colon to select between previous
(history) values. It also has functions to manipulate the history and the "current" value, or
press colon ":" to select a previous value.


The readLine() is much simpler, and allows for empty input, as Enter
doesn't mean "last value" as it does for Input.


The optional default value parameter to the P() expression for grabbing parameters to
functions, can be used to produce functions that ask for missing values.

```
P(1,Input("Enter value").get) =>value ...
```
# Block expressions


The traditional blocks inside curly braces come in three variants in CFT.

## Local blocks


Local blocks are just for grouping code that runs in the same context ("code space")
as the code around it. Technically they are considered expressions.

```
if (a>b) {
...
}
```

Local blocks can contain loops, but can not be split into multiple code spaces using the PIPE ("|")
symbol, as they execute in the same run-context as the code around them. Any calls to out() or report()
add to the result list of the environment.

## Inner blocks


An Inner block is a separate "code space", where we can do loops and call out() without
affecting the result of the caller.


They are like calling an inline function (no parameters though), as their inner
workings do not affect the caller, 
**except** that they have access to, and can
modify local variables.

```
# List number of lines containing a pattern, in files under current dir
# --
	P(1,readLine("pattern"))=>pattern
	Dir.files->f
		count=Inner{
				f.read->line
				assert(line.contains(pattern))
				out(line)
			| _.length
		}
		report(f.name, count)
/CountMatches
```

Apart from how this example could have been implemented much better with Grep.fileCount(), this
is an illustration of how Inner blocks are more general and powerful than using
the PIPE to split function bodies into code spaces.


Of course, inner blocs can themselves be partitioned into code spaces, as we see
in the above example, and in turn contain blocks ...

## Lambdas


A Lambda is an object (a value) that contains a block of code, so it can be called, with parameters. The code
inside runs detached from the caller, and behaves exactly like a function.

```
Lambda{P(1)+P(2)}
/MyLambda
MyLambda.call(1,2)
```

Can be used to create local functions inside regular functions, but mostly used to create
closures and/or Dict objects / classes.


The above vode defines a regular function, MyLambda, that returns a Lambda object, which can
then be called with the .call().

## Block expressions summary


Local (plain) blocks for non-PIPE-separated blocks of code, typically used with "if". Running in
the same code space as outside the block, which means it can contain calls to break() and out() as well as
assert() and reject() and affect the (innermost) loop of those outside the block.


Inner blocks for isolated processing loops inside other code. This means that calling
out(), assert(), reject() and break() inside, has no effect on loops outside the block. They
can be split into parts with PIPE, just like functions.


Lambdas are callable functions as values. There is a lot more to say about Lambdas and their
close cousin, the Closure, later in this document.

## Local variables scope


Local variables inside functions (and local / Inner blocks) all share the same scope. This
means that there are no sub-scopes inside local or inner blocks inside a
function body.

```
{x=3} x
3
```
# List filtering with Lambda


Instead of using processing loops, filtering lists can also be done
using .filter() function of the List object.

```
"12345".chars.filter(Lambda{P(1).parseInt}).sum
/t
```

Here, the lambda is used to convert strings to int values.

## Removing items


To remove items from the list, we let the Lambda return null.

```
"12345".chars.filter (Lambda{ P(1).parseInt=>x if(x>=3,x,null) })
```

This returns a list of 3,4,5

# Conditionals - if expression


Conditional execution of code is done in two ways in CFT, with the first being how we
control processing loops with assert, reject and break.


Then there is the if-exression. It takes two forms, but is always considered an expression, not a statement.
The difference between expressions and statements, is that expressions always return a value, which statements need not.

### Inline form

```
if (condition, expr1, expr2)
if (condition, expr1)
```

The first selects between the two expressions, based on the condition, evaluating and returning
expr1 if condition is true, otherwise expr2. The second conditionally evaluates expr1, or if
the condition is false, returns null.

### Traditional form

```
if (condition) stmt1 else stmt2
if (condition) stmt1
```
### Example 1


Inline form. Check if some value is null, and if it is, provide a default value

```
if (value != null, value, "defaultValue") =>value
```
### Example 2


Using traditional form to call statement "break".

```
i=1
loop
out(i)
if (i>=10) break else i=i+1
```
### Expressions are statements ...


Note that all expressions are also statements, which means the first example can be
written on traditional form:

```
if (value != null) value else "defaultValue" =>
 value
```
### Blocks are expressions ...


Also note, that (local and Inner) blocks are expressions, which can contain statements, so we can do this:

```
i=1
loop
	out(i)
	if (i>=10,{break},i=i+1)
```

Or this, which is perhaps the most readable:

```
i=1
loop
	out(i)
	if (i>=10) {
		break
	} else {
		i=i+1
	}
```
## if-ladders


The implementation in CFT supports chaining multiple if after each other.

```
if (condA) {
...
} else if (condB) {
...
} else if (condC) {
...
}
```

Decoding some value x into a numeric code, we can enter the following

```
code = if (x=="a") 1 else if (x=="b") 2 else if (x=="c") 3 else 4
```

Note: the above is not very elegant. It might be better populating a
dictionary or something.

# Lazy evaluation

### Lazy if


The if-expression uses lazy evaluation, which means that only the selected
value expression (if any) gets evaluated. This is the same as every other
language.

### Lazy AND, OR - &amp;&amp; ||


Boolean expressions with logical AND and OR, are lazy, again as in
every other language.

### Lazy P(N,defaultExpr)


The P() expression to access function parameters only evaluates the default
expression if function parameter N has no value or null-value.

# The error() function


The error() global function contains a conditional part, and if true, throws
a soft error with the string part, terminating current execution. Alternatively it can
be used without the condition, which means it will usually be used together with "if".

```
error(1+1 != 2,"this should not happen")
if (1+1 != 2) {
	error("oops again")
}
```
# Output to screen

```
println("a")
println("a",a,"b=",b)
```
# Running external programs

## Summary


The functions for running external programs are part of the Dir object, which defines the
working directory for the program.

```
Dir.run ( list|...)
Dir.runCapture ( list | ...)
Dir.runDetach ( list|...)
Dir.runProcess ( stdinFile, stdoutFile, stdErrFile, list|... )
```

The parameters written as "list|..." means either a List object, or a list of
String values, separated by comma.

## Dir.run()


This command is used for running external programs in the foreground. What this means is that if
the program requires user input, we can give it, and the CFT code will not continue until
the external process has terminated.

```
Dir.run("cmd","/c","git","pull","origin","master")
```

Many Windows programs require the "cmd","/c" in front of the actual program.
For proper operating systems (Linux) you naturally skip the two first elements of the command list.

## Dir.runCapture()


This works the same as Dir.run(), but returns a List of strings representing stdout from the
external program, to be processed further. Not suited for interactive use.

```
Dir.runCapture("which","leafpad") =>lines lines.length>0 && lines.nth.contains("leafpad")
/HasLeafpad
```
## Dir.runDetach()


Use to run external program in the background. The CFT code continues running after forking
off the background process. Nice for editors etc.

```
Dir.runDetach("leafpad", Sys.savefile.path)
```

This example runs the leapad editor in the background, with the path of the current savefile as
argument.

## Dir.runProcess


Runs external program, reading input lines from text file, and deliver stdout and stderr to
files. Returns an ExtProcess object, which is used to monitor, terminate or wait for the
external process to finish.


The complexities of creating and removing temporary files, is encapsuled in the library
function 
**Lib:runProcess**, which in turn is called from the simpler 
**Lib:run** function, which
also handles waiting for the external process to finish, before returning.


Both of the Lib functions take the same four parameters, but often only the first is used, as the
rest have useful defaults.


**Lib:run** is the notation for calling a function in another script.

## Lib:runProcess utility function


This is a CFT function in the Lib script, which hides the complexities of
calling Dir.runProcess (above).

```
Lib:runProcess(List("ls","-l)) => result
```

The result object is a Dict with various system info, representing the running
process. It has two closures of interest.


A **closure** is a lambda, with a "self"-reference to a dictionary, and
so acts like a "member function" of that dictionary, as it has access to data and
other closures of that dictionary object.


In this context we usually refer the closures with dotted notation, which
means they are automatically called.
```
result.isCompleted     # returns boolean
result.wait            # waits for process to finish, then returns another Dict
```

The result.wait closure, when called, returns a Dict with the following content:



- cmd - the command (list)
- stdin - the stdin lines (list)
- stdout - stdout lines (list)
- stderr - stderr lines (list)
- exitCode - int



To show the Lib:runProcess function code

```
?Lib:runProcess
```
## Lib:run utility function

```
Lib:run (List("ls","-l")) => result
```

The implementation of Lib:run consists of calling Lib:runProcess and then
calling the wait closure, as seen above, returning the result from that call.


To show the Lib:run function code

```
?Lib:run
```
## Work directory issues


For external programs that depend on running from a specific directly, either navigate to current directory
interactively, or just let your code call the .cd() function on some Dir object before
calling Lib:run.

```
Dir("/home/user/xyz").cd
Lib:run(...) => result
```
## Doing ssh


If you need to run SSH commands on remote targets, use the SSH library script, which
contains two major functions: run() and sudo(), These call Lib:run then filter the output
to stdout using a marker to eliminate the welcome text when logging in etc.

### Side note: ssh without password


To set up ssh login without password, create and distribute an ssh key, then
copy it to the target host, as follows (in Linux shell).

```
ssh-keygen -t rsa
ssh-copy-id user@host
```
# Synthesis

## Creating code from values


The 
**syntesis** functionality comes in three variants. Two of them are "colon commands".



- The :syn command syntesizes code from the last result.


- The :NN  (where NN is an integer) syntesizes the indicated element of the last result list. If
last result is not a list, you get an error.


## Example using :syn

```
Dir.sub("src")
<obj: Dir>
src/

:syn
synthesize ok
+-----------------------------------------------------
| .  : Dir("/home/roar/CFT/src")
+-----------------------------------------------------
Assign to name by /xxx as usual
/DirSrc
```

Running the code Dir.sub("src") creates a Dir object for the src subdirectory (regardless of
whether it exists or not).


When we run the ":syn" command, and as CFT remembers the last result value, it creates code for it.


If this succeeds, it inserts the generated code line tino the "code history", pretending
it was a command given by the user, which means it can now be assigned a name, for example "DirSrc"


Calling function "DirSrc" will now always return a Dir object pointing to the same
directory, as the specific value was turned into code, not taking into account how the value
was created (and that Dir without params returns current directory, which may change).


Having synthesized a value makes it independent of how it was created, which
in this case means that DirProject always returns that specific directory, regardless
of current directory.

## Example using :NN


To synthesize a single element when the last result was a list, use :NN, as follows

```
ls
<List>
0: runtime/              | d:2 | f:12
1: CallScriptFunc.java   | 1k  | 1398  |             | 2020-02-28 22:38:09
2: CodeHistory.java      | 10k | 10980 | 2d_22:23:24 | 2020-06-07 12:57:49
3: CommandProcessor.java | 0k  | 365   |             | 2020-02-28 22:38:09
:2
synthesize ok
+-----------------------------------------------------
| .  : File("/home/roar/Prosjekter/Java/ConfigTool/src/rf/configtool/main/CodeHistory.java")
+-----------------------------------------------------
Assign to name by /xxx as usual
```

If the last value was not a list, the ":NN" command will fail with an error.

# Output format / Term object


Output to screen is regulated via a Term object. It is a session object, remembering the
current size of the terminal window.

## The @term shortcut


After resizing the terminal, we need to update the Term object.

```
@term
```

This works on Linux (using stty command) and on Windows (powershell).

## Line wrapping


By default, ouput line wrapping is off, which means that lines longer than the Cfg.w gets truncated
with a '+' to indicate there is more. It can be switched on/off via the Cfg object, but there is also a
colon command ":wrap" which toggles wrapping on or off.

# Templating


Templating is the task of merging data into text, or alternatively of selecting
blocks of text to form a custom result.


This is useful for producing configuration files, generating code, and similar.

## Merging text with Dict


This was the original implementation, but it has mostly been replaced by the
ability to merge output from expressions directly into text. Skip to the
section for *.mergeExpr* for for details about this.


To merge values into a template, we may use a dictionary object (Dict) combined with the
merge() function of strings. This replaces occurrences of names in the dictionary
with their values (as strings).

```
Dict.set("name","Julius")
/data
"Dear name".merge(data)
<String>
Dear Julius
```
### Dict.mergeCodes()


The merge is based on a direct match. Often we like to mark our merge codes. The Dict
object has a function, ".mergeCodes()", which returns a new Dict object, where all names of fields
are rewritten into ${name}. Changing the template correspondingly, this eliminates the risk of
accidentally matching text not meant as merge codes.

```
Dict.set("name","Julius").mergeCodes
/data
"Dear ${name}".merge(data)
<String>
Dear Julius
```
### Custom merge codes


The Dict.mergeCodes() also take an optional two parameters, which are the pre and post
strings for creating the merge codes. Note that either none or both of these must be
given.

```
Dict.set("name","Julius").mergeCodes("[","]")
/data
"Dear [name]".merge(data)
<String>
Dear Julius
```
### Example using raw strings and Sequence


**Raw strings** are a special notation for strings, that is as follows:

```
a = @ this is a "raw string" ...
```

The raw string starts following the "@ " prefix, and continues to the end of the line.


**Sequence()** and 
**CondSequence()** are built-in expressions that are similar to
List, in that they create list objects, but with a relaxed syntax, which means commas
between values are optional.


The CondSequence() is conditional, which means if the first parameter is false, it generates
an empty list. Since lists can be concatenated with "+", we can do this:

```
	P(1) => replSetName
	P(2) => clusterRole
	SymDict(replSetName, clusterRole).mergeCodes =>data
	isConfNode = (clusterRole=="configsvr")
	Sequence(
		@  :
		@  :
		@ replication:
		@   replSetName: '${replSetName}'
		@
	) + CondSequence(isConf,
		@
		@ # NOTE: config replica set node
		@
	) + CondSequence(!isConf,
		@
		@ # NOTE: data shard replica set node
		@
	)+Sequence(
		@ sharding:
		@   clusterRole: '${clusterRole}'
	)
	->line out(line.merge(data))
/CreateMongodCfg
```
## .mergeExpr


The List.mergeExpr is another option. Instead of merging data from a dictionary
into the template, the template contains CFT expressions inside << and >>.

```
# Create template
# --
	P(1,"John Doe")=>name
	P(2,List)=>hobbies
	Sequence(
		@ Dear <<name>>
		@ We see that you have <<hobbies.length>> hobbies:
		@ << hobbies->h out("   - " + h) >>
	).mergeExpr
/GetTemplate
```

Expressions can include iterating over a list, as seen, and creating Inner
blocks, if we want to use PIPE inside. Usually we will probably refer variables,
or call functions.


Note: the .mergeExpr function optionally takes two parameters for start and stop
symbols, similarly to the .mergeCodes of Dict, so we can do this:

```
Sequence(
@ Dear [name] ...
).mergeExpr("[","]")
```
# Text processing


In the section on templating, we used the Sequence() and raw strings, to represent text. This
section summarizes the different ways of working with text.



- Reading separate text files
- "here" documents in script files
- DataFile
- Sequence() and raw strings


## Reading text files


Any text file can be read and processed line by line as follows.

```
Dir.file("x.txt").read->
line ...
```
## Script file "here" documents


This is a special feature of script files, where a sequence of lines between two
marker lines, are translated to a List expression on the fly, as the script file is loaded.

```
<<<<<<<<<<<< Identifier
This is
some text
>>>>>>>>>>>> Identifier
/myTemplate
```

Calling the myTemplate function from the interactive shell, produces the following result

```
myTemplate
<List>
0: This is
1: some text
```

The markers need at least 3x of the '<' or '>' followed by space and a matching identifier.

## DataFile


The DataFile global function reads a special text file with individual sections identified
by a user selectable selector string, and names.


Example data file 'data.txt'

```
### A
This is
template A
### B
This is
template B
```

The code to use this file consists of creating the DataFile object, passing the separator
string as a parameter, then accessing the individual templates.

```
DataFile(File("data.txt"),"###")
/df

df.get("A")
<List>
This is
template A
```
### Include blank lines


The function DataFile.get() returns only non-blank lines. To get all lines, use function getAll().

### Allowing comments


DataFile has support for comments in the template text, which can be automatically
removed. They are defined by another prefix string as follows:

```
DataFile(someFile,"###").comment("//")
/df
```

Now all lines starting with "//" are automatically stripped from any output.

## Sequence() and raw strings


Raw strings start with '@' and consist of the rest of the line.
In addition, two expressions have been added, called Sequence() and CondSequence(). These
generate List elements, but with a relaxed syntax, making commas between values optional,
for max readability when creating a template.

```
Sequence(
	@ header
)+CondSequence(<condition>
	@ optional-part
)+Sequence(
	@ footer
)
```

What happens here is that the Sequence() and CondSequence() expressions result in List
objects, which are then concatenated via the "+" operator.


The "@" type "raw" strings can be used in regular code as well. The behaviour is as follows:


- When "@" is followed by single space, that single space is removed from the string
- When "@" is followed by another "@", all characters following that sequence becomes the string
- When "@" is followed by anything else, all of the rest of the line becomes the string



The advantage of this notation over that of "here"-documents, is that it allows proper
indentation, for greatly improved readability in script code, and that it's easy to
add conditional blocks, while the "here"-document means we can paste original text
directly into the script code.

# Processing JSON


The JSON script handles parsing JSON into a Dict/List structure, and
conversely, takes a Dict/List structure to JSON format again.


The JSON library is a CFT script, which means it is written in CFT, using the
Lib.Text.Lexer to tokenize the input, and a plain recursive-descent parser as
functions inside the JSON script.


Parse example:

```
JSON:Parse("{a:1,b:[2,3,4]}")
Dict: a b
/x
x.b
0: 2
1: 3
2: 4
```

Generate JSON example

```
Dict.set("a",1).set("b",2).set("c","xyz".chars)
Dict: a b c
/y
JSON:PP(y)
0: {
1:   "a": 1,
2:   "b": 2,
3:   "c": [
4:     "x",
5:     "y",
6:     "z"
7:   ]
8: }
JSON:Export(y)
{ "a": 1, "b": 2, "c": [ "x", "y", "z" ] }
```

As of version 2.3.6 dictionaries keep track of the order values are stored, which ensures
that the order of fields added to dictionary, is the order of fields when generating JSON.


Vice versa, the order of fields inside objects in a JSON string, when parsed, becomes the order of
fields in the corresponding dictionaries.

# Processing XML


The "XML" script handles parsing and pretty-printing XML. It also has functions for constructing
an XML object with code.

## Parsing XML


Call XML:Parse with list of strings, or single string. Then call .PP (pretty-print) on the
root node, to see the result.

```
# XML:t1
rootNode = XML:Parse(XML:exampleXML).first
rootNode.PP  # pretty-print
```
## Creating structure with code

```
# See XML:constructedObject
# and XML:t4
root = XMLNode("root")
root.attributes.pi="3.14"
root.addText("root test before A")

# add sub-node with name and populate it
a=root.sub("A")
a.attributes.inner="a"
a.addText("a-text")
root.addText("root test after A")

# and another sub-node with two inner nodes
b=root.sub("B")
b.sub("C")
b.sub("D")

# Show structure
root.PP
```
## Looking up content


Use the .attrGet(name) for attributes that may not exist. It will then return null, while
using dotted lookup will fail with error, if name not found.

```
# Assuming object built in example above
root = XML:constructedObject
root.attrGet("pi"))  # "3.14"
root.subNodes("A").first.attrGet("inner") # "a"
root.attrGet("does-not-exist")  # null
```

Show available data and functions for the XMLNode object:

```
XML:Show
```
# Internal web-server


There is a small embedded web-server in CFT, available in the Lib.Web object. It
supports GET and POST, does parameter and form input parsing.


See WebTest script. Running the Init function sets up a very simple web "site" on
port 2500 on localhost.


Remember to re-run Init after changing the code, as the
web-server runs as a Java background thread, and needs to be updated, in order to serve new
content.


A bit of a work in progress, not terribly useful, bit it sort of works.

# 3D library


Included and adapted an old 3d-library written by me, over 20 years ago, for rendering 3D scenes,
working purely in Java, without any acceleration. It is slow, but gets the job done.


It is non-interactive, code only, and fits well into CFT.

## Ref


The key object is the Ref, which
is a complete coordinate system living in global 3D space. It has functions for moving
in different directions, as well as rotating around its three axes, and scaling.

## World


Then there is the World object, which represents the camera and renderer. By default, the
"film plane" of the camera lives at position (0,0,0) in the global coordinate system, and is defined using
millimeters. This means that using a Ref object without setting scale, means it operates
in millimeters. But we can change the scale on Ref objects, for example setting it to
1000, which means that the units are now meters.


Note that Ref objects are immutable, so all calls to Ref functions create new Ref objects.

```
world = Lib.DDD.World
ref = Lib.DDD.Ref  # at 0,0,0 and scale=1, which for default camera defs means millimeters
ref=ref.scaleUp(1000)  # now working in meters
ref=ref.fwd(3).turnRight(30).down(0.5)
# new ref is placed 3 meters forward, turned right 30 degrees, and lowered 0.5 meters
```
## Brushes


The DDD library uses 3D brushes to generate content. A brush is defined as a sequence of
line segments, for example a square, which is then "dragged" through 3D-space in a
sequence of "pen down" operations.

```
boxBrush = world.Brush.box(1,1,Lib.Color(255,0,0))
# creates a brush for a box centered on origo, with a certain size and color
# note the size here is "relative" and determined by the scale of the Ref's used
# when painting with the brush.
ref = Lib.DDD.Ref.setScaleFactor(1000).forward(3) # forward 3 meters
boxBrush.penDown(ref)  # start drawing with brush
boxBrush.penDown(ref.fwd(2))  # move 2 meter forward and do penDown here
boxBrush.penUp
```

The above code generates a red box 1 x 1 meter in cross-section and 2 meters long.

## Rendering


After doing some number of brush operations, it is time to create a PNG file
with the image, as seen by the camera.

```
file=Dir.file("test.png")
world.render(file)
```
## Drawing a spoked wheel


The example script DDDExample contains code for rendering a spoked wooden wheel.


**
# 2D library


Similar to the 3D library, the 2D library lets us draw vector graphics using a
2D Ref object that moves in the plane, using either lines or filled polygons.

## Spoked wheel


Created DDExample which draws the same wheel as the 3D version, using the LineBrush of DD.World


**

Created DDExample2 which uses polygon drawing Brush of DD.World.

**# Command line args


If CFT is invoked with command line arguments, the first is the name of the script,
that is, a savefile minus the "savefile" prefix and ".txt" ending.


Then follows zero
or more command lines, on string format. For values containing space or otherwise
have meaning to the shell, use quotes. Example:

```
./cft Projects Curr
```

This loads script Projects, then calls the Curr function inside.

```
./cft
./cft scriptName [commandLines]*
./cft -version
./cft -help
./cft -d scriptDir [scriptName [commandLines]*]?
```
# Environment variables


Available via Sys.environment() function.

# Debugging


After editing a script, normally you need to test all functions even for basic syntax
errors, as CFT functions are parsed only when run.

## @lint


A simple lint function is available in Sys.lint. It checks that all functions in current
script parses ok. It is mostly invoked using a shortcut:

```
@lint
```
## addDebug()


The global addDebug() function takes some string, and creates a log line that also contains
the source location, and adds this to the current stack frame of the CFT call stack.


This means no output is displayed until something goes wrong, and the CFT stack trace
is displayed.


## Sys.getCallHistory

This function returns the call history as a list of text lines, which can be logged or displayed.


## setBreakPoint(str)

This statement stops execution, and shows call history, and asks if we want to abort ("y") or
not (Enter).



# Working with pasted text lines from stdin


If you've got some text in the copy-paste buffer that you want to work with, the
readLines() global functions can be used. It takes one parameter, which is an end-marker, which must
occur alone on a line, to mark the end.


The readLines() function returns a list of strings, which you can turn into code and save under
some function name, using synthesis.

```
readLines("XXX")
(paste or enter text, then enter end-marker manually)
XXX
<List>
0: ...
1: ...
:syn
/someName
...
```

The ":syn" command synthesizes the List object, and we then create a function "SomeName" for it. If we save the
script, we can now run SomeName to produce the list of lines pasted or entered.

# Differing between Windows and Linux


Calling function Sys.isWindows() is used to differ between the two in code. It does this
by checking if (Java) File.separator is a backslash.

```
Sys.isWindows
<boolean>
false
```
# Predicate calls


Example: to decide if a string is an integer, without
resorting to either creating a built-in predicate function like .isInt, or even
using regular expression matching, there is the 
**predicate call** functionality,
where one calls a function in a special way, resulting in a boolean value that tells
if the call was ok or not.


All dotted calls are made into predicate calls, by adding a '?' questionmark between the dot
and the function name.
```
"sdf".?parseInt
<boolean>
false

"123".?parseInt
<boolean>
true
```
# The onLoad function


In order for scripts to run code as the script is loaded, we can define a function
called onLoad, which is called every time the script file is loaded or reloaded.

# Error handling


Exception handling in CFT is split into two parts, reflecting two types of
situations:



- CFT logical or data errors, called 
**soft errors**

- General errors, stemming from underlying Java code, network situations etc, called 
**hard errors**


## Soft errors


Soft errors are created by calling the error() function.


They can be specifically
caught with tryCatchSoft(), which returns a Dict containing either:

```
ok: true
result: ANY
or
ok: false
msg: string
```

Hard errors propagate right through tryCatchSoft().

## Hard errors


Hard errors are all kind of error situations arising from the Java code running CFT.


The tryCatch() expression catches both hard and soft errors, and returns a Dict containing

```
ok: true
result: ANY
or
ok: false
msg: string
stack: List of string
```

An example of a hard error is trying to access a variable or function that doesn't
exist.

# Get type of value


The global function getType() takes one parameter, and returns
the type name of that value, as a string

```
getType(3)
<String>
int

getType(Dict)
<String>
Dict
```
# Closures and dictionary-objects

## Manual closure


A closure is a Lambda that is bound together with a dictionary object. The dictionary object is
available via the automatic variable "self" inside the lambda (now closure).

```
data=Dict
closure = data.bind(Lambda{
	P(1) => value
	self.value=value
})
closure.call("x")
data.value   # returns "x"
```

Here we create a dictionary. We then call .bind with a lambda, which produces a closure.


If we now pass the generated closure as a parameter to some function that expects a lambda or closure,
commonly type-identified as "Callable", when it is called with a single parameter value, that value
gets stored inside the data dictionary, which means we can access it after the function we called has returned.

## Dictionary objects


Another way of creating closures, is by storing lambdas into dictionaries. The lambda gets automatically
converted to a closure, which refers the dictionary.

```
data=Dict
data.value=null
data.f = Lambda{
	P(1) => value
	self.value=value
}
closure=data.get("f")

callSomeFunctionWithIt(closure)

# get value passed when that function called the closure
data.value
```

The "data" dictionary now is a simple object, which contains both a value and a closure.

## Auto-invoke


Lambdas and Closures, when stored in a variable, are invoked with .call(...).


Obtaining a Closure from a dictionary with dotted notation, however, implies an automatic invocation,
with or without parameters.

```
data=Dict
data.f=Lambda{println("called f")}
data.f   # prints "called f" to the screen.

# To get a reference to the closure, we use the .get() function of Dict
data.get("f")    # returns Closure object without invoking it
```


# Type checking with "as"


**v3.2.5**

The getType() has frequently been combined with error() to do type checking of parameters.

```
P(1)=>x
error(getType(x) != "String")
```

The problem, is that it is easy to mistype, like getType("x") which of course always is "String".


The "as" syntax simplifies this to the following:

```
P(1) as String => x
```

A secondary form exists, where instead of an identifier type, such as String in the example, we take allowed type name(s) from
an expression. It must be written inside ()'s. If it returns a list, then it's assumed to be a list of valid type names.

```
type="String"
P(1) as (type) => x

# or
types="String int".split
P(1) as (types) ...

# or even
P(1) as ("String int".split) ...
```

If an "as" fails, a hard error is thrown, with details about what was expected, and what was found.

### Null-values


The type of null-value is "null". To allow a value to be optional is to allow it to be of type "null". Since
this is a common thing to do, we can add a '?' after the type (identifier or expression inside parantheses), which
simply means "or null":

```
P(1) as ("String null".split) => optionalStringValue

P(1) as String? > optionalStringValue  # The '?' means "or null"
P(1) as ("String int".split)?    # String, int or null
```
### Dict (type) names


The Dict object has an optional name property, either set at creation by Dict("something") or via Dict.setName("something").

This name can be filtered in the "as" expression, prefixing the type identifier or type expression inside ()'s with
an & (ampersand) as follows:

```
# Accept dictionary named as MyData only, or null
# --
	P(1) as &MyData? => data
	...
/f

# Create such data
# --
	Dict("MyData")
		_.a=1
		_.b=2
		=> myData
		
	# Call f with this object
	f(myData) # should match "as" type of "f"
/test
```
### Closures and Lambdas


Both lambdas and closures are assigned the type "Callable", which simplifies type checking, as they have identical
.call() functions to invoke.


Mostly a function does not need to know if an assumed Lambda is in fact a Closure.


Still, if one needs to detect if a value is a lambda or a closure, it's easiest done by checking for the presence of
one of the functions that closures implement, and lambdas do not, for example the .lambda function of closures,
to obtain the lambda component.

```
P(1) as Callable => callable
isClosure = callable.?lambda  # returns true if value has function lambda, which means it is a closure
```


### Dotted lookup => auto call

When referring a lambda or closure as part of something else, that is, using "." in front of it, it is
invoked directly with or without parameters, so no ".call" then. Referring a lambda or closure via a parameter or
local variable, and wanting to invoke it, use ".call" with or without params.

Also note that ALL lambdas stored in dictionaries are converted to closures.

```
d=Dict
f=Lambda{
	P(1) as String => name
	"hello " + name
}
d.f=f

f.call("Bob")  # no dotted lookup of lambda, so use .call() to invoke it
d.f("Bob")  # dotted lookup (".f") means we invoke closure without ".call"
```

To access a closure from within a dictionary, without calling it, use instead the .get() function

```
f2=d.get("f")  # returns lambda (closure)
```

The closure can now be passed as an argument to some function somewhere, who uses .call() to invoke it.

# Classes


v3.5.0


CFT supports primitive classes, which are really just dictionaries with closures for member functions,
combined with the name attribute of all Dict objects, created by Dict(name) or Dict.setName(name), which we can check
for in the "as" type checks with &amp;name.

Inheritance is supported using the Dict.copyFrom function, or any other means of copying
data and lambdas from one Dict to another. as there is no "class definition", only a
block of code (class function) that sets up the class object.


Classes are functions, and are defined similarly to functions, by code followed by
slash something. Defining a simple class MyClass:

```
# my class
# --
	P(1) as int => x
	self.x=x
/class MyClass
```
## Class object types


There is no differentiation between classes and objects. 

A class function returns an object.
Inheritance is possible by some class function instantiating a class object of some other type,
and incorporating elements from it into itself, possibly using the Dict.copyFrom(anotherDict)
function, as the "self" object inside an object is a Dict.


The OODemo script examplifies this, and also employs the second version of the /class line,
where we supply the type explicitly.

```
# Common Val class
# --
	P(1) as String => type
	P(2) as (type) => value
	...
/class Val

# Val class for processing int
# --
	P(1) as int => value
	self.copyFrom(Val("int",value)) # "inheritance"
	...
/class ValInt as Val
```

## What a "/class" function does


There is no problem creating dictionary objects without using /class functions.

```
# TypedContainer class (using /class)
# --
	P(1) as String => type
	self.type=type
	self.value=null
	self.set=Lambda{
		P(1) as (self.type) => value
		self.value=value
	}
/class TypedContainer

# Create custom subclass of TypedContainer inside normal function (without using /class)
# --
	self=Dict("TypedContainer")
	self.copyFrom(TypedContainer("int"))
	self
/IntContainer

# Test it
IntContainer.set("test")  # Fails with error, expects int
```
# Dict set with strings


Reading name-value assignments from a property file or similar, is best done via the .setStr()
function on the Dict object. It strips whitespace and accepts both colon and '='.

```
Dict.setStr("a : b").setStr("c=d")
/d
d.get("a")
<String>
b
```

To process a property file, assuming commented lines start with '#', we can do
this:

```
# Read property file
# --
	P(1) =>propFile
	Dict =>d
		propFile.read->line
		reject(line.trim.startsWith("#"))
		assert(line.contains(":") || line.contains("="))
		d.setStr(line)
	|
	d
/GetProps
```

# Dict.get with default value


The Dict.get() method takes an optional default-value which is returned if no value
associated with the name, but in that case the default value is 
**also stored** in the
dictionary.

```
data=Dict
data.get("a",3)  # returns 3
data.keys        # returns list with "a"
data.get("a",5)  # returns 3 as it was set above
```
# Dict.ident=Expr


**v3.3.0**

Extended parser so that we don't have to use Dict.set() for values with a name that are valid identifiers:

```
data=Dict
data.name="test"
```

The return value from this assignment is the Dict object. Multiple assignments can be chained using
the underscore ("_") global function, which returns the value on the data stack:

```
Dict.name="test" _.role="manager" _.age=40 =>
 data
```
# List.nth() negative indexes


Using negative indexes to List.nth() counts from the end of the list. Using value -1 returns the
last element, -2 the second last, and so on.

```
List(1,2,3,4).nth(-1)
<int>
```
# Function parameters ...


In addition to grabbing one parameter at a time, using P(pos), we can also process the
parameter values as a list and as a dictionary.

## Get parameters as List


The function parameter expression P() when used with no parameters, returns a list of
the parameter values as passed to the function.

```
# Some function
# --
P => paramList
	...
/example
```
## Get parameters as Dict


The PDict() expression takes a sequence of names to be mapped to parameters by position,
resulting in a Dict object. Missing values lead to the special value null being stored
in the dictionary.

```
# Some function with three parameters
# --
	PDict("a","b","c") => paramsDict
	...
/example
```
# The general loop statement


In addition to looping over lists, there is a general loop construct. It identifies no
loop variable, and loops forever, until break() is called. It also obeys assert()
and reject() as with list iteration.

```
a=0 loop break(a>3) out(a) a=a+1
<List>
0
1
2
3
```

If you forget to increment the variable a, or forget or create a valid break(), then
the loop may never terminate, and CFT has to be killed with CTRL-C 

# Storing CFT data structures to file - syn() and eval()


A  persistent solution for storing data is to store a data structure to file. This is done using
the synthesis functionality, which is made available as a global function as well as the
"colon command" used before. This means we can write huge lists and sets of files and
directory objects to file, and restore it later, without going through possibly time
consuming computations.


To restore the structure, we use the global eval() function.

```
# Save data to file
# --
	P(1)=>file
	P(2,"data") =>data
	file.create(syn(data))
/saveData

# Restore synthesized data
	P(1)=>file
	eval(file.read.nth)
/restoreData
```

This can be used to save arbitrarily big structures, as long as they are synthesizable.

# The CFT database


CFT implements its own primitive database, as found in Lib.Db.Db2, and which is usually
interfaced via the Db2 script.

```
Db2:Set("myCollection", "field", "test")
```

The Db2 persists data to file, and handles all values that can be synthesized to code.


Also there is a Db2Obj script, which saves data objects identified by UUID's, which are
made by calling the Lib.Db.UUID function.

## Collections


Apart from using static strings as collection names, another common practice is
to use the scriptId, which is a function of the Sys object:

```
Db2:Set(Sys.scriptId,"name","value")
```

Sys.scriptId is better than Sys.scriptName, since there may be multiple scripts with the
same name (in different directories).

## Lib.Db.Db2 vs Db2 script?


The Db2 script uses the Lib.Db.Db2 object. The difference is that an object is implemented
in Java, while the script is code that runs in the interpreter. See separate section on "Objects vs Scripts".

## Synchronization


Calls to the Db2 database are thread safe, via a Db2 lock file per collection,
to prevent parallel updates, or partial reads etc.


In order to create transactions consisting of multiple Db2 calls, the Lib.Db object
contains support for named locks. Example:

```
Lib.Db.obtainLock("Unique Lock Name",5000) ## 5000 = wait max 5 seconds before failing
Db2:Get(Sys.scriptId,"someValue") => data
# (modify data)
Db2:Set(Sys.scriptId,"someValue",data)
Lib.Db.releaseLock("Unique lock name")
```

# Lib object vs Lib scripts


As for the case of two Db2 entites above, with a script and an object of the same name,
this is also the situation for "Lib", where the (global) function "Lib" produces a Lib object,
while there also exists a Lib script.


The Lib object, like all CFT built-in objects, has its functions implemented in Java, while the
Lib script is a text file with functions written in the CFT programming language, which are
interpreted by Java when called.



Getting information about functions inside objects and scripts differ as follows:

```
# Lib object content

Lib help


# Lib script content

?Lib:

# or

:load Lib
?
```

Calling functions inside objects and libraries differ as well.

```
Lib.Text               # call function Text inside Lib object
Lib:Header("Hello")    # call function Header in script
```
# onLoad functions


Calling a function onLoad results in it being called every time the script is loaded
and (automatically) reloaded in GNT. Nice for clearing defaults, when new session.


Each individual session is identified by a session UUID, which may be stored in the
database, so the onLoad can detect this and reset state in the database.

# Multitasking in CFT


CFT offers the ability to run multiple processes of CFT code, via the SpawnProcess() expression.


It takes two or three parameters, a context dictionary and an expression, with
an optional lambda or closure, which if defined gets called with the Process object as parameter
whenever there is new output or the process has terminated.


The named values from the
context dictionary become local variables when the expression is executed, which takes
place in a separate process.


The code runs in a virtualized environment. Output is buffered inside the Process object, and
if the code requires input, it will block, until we supply input lines via the Process
object.

## Key concepts


CFT objects (as implemented in Java) are generally not thread-safe, but it turns out that they don't
need to be, as a running CFT thread has no global state. There are no global variables, only in-function
local variables.


The only way a CFT thread can maintain persistent (global) state, is using external storage,
usually via synthesis and eval, such as implemented by the Db2 internal data storage. What this means
is that data written to Db2 is converted to code. Reading the data back from the database
means running that code (with eval), and get a newly created data structure.


Logically, this corresponds to 
**message passing**, in that the writer (sender) and reader (receiver)
of data are loosely coupled, and that any receivers will always create local copies of the data.


Race conditions is still possible with regards to external elements such
as the file system, FTP servers, other databases, etc.

### Intended use


It should be added that the intended use of spawning processes in CFT is for parallelizing
multiple possibly time consuming jobs, each operating invididually, then collecting the
results. Typical examples are mass updating tens of virtual machines, as well as getting
various stats from sets of hosts.


The optional third parameter (lambdaOrClosure) is a way of processing both output via
the process stdio object (which is virtual), and capturing the result value when
the process has terminated.

## The Process


The Process object represents a separately running thread, executing some CFT expression, in a
separate environment. Its standard I/O is virtualized by the Process object.


Listing Process functions:

```
SpawnProcess(Dict,1) help
# close() - close stdin for process
# data() - returns the (original) context dictionary
# exitValue() - returns exit value or null if still running
# isAlive() - true if process running
# isDone() - true if process completed running
# output() - get buffered output lines
# sendLine(line) - send input line to process
# wait() - wait for process to terminate - returns self
```
## Example: pinging a list of hosts


The following example show how we can perform system monitoring efficiently using
SpawnProcess.


This example is about pinging a set of servers, to see which are up. We start by creating
a function for this, and testing it manually.

### Create and test regular ping function


It is okay to print out a lot of stuff, as all of that will be collected when
calling function inside a Process. We will log that to the database if
function returns false.

```
# Ping host, return boolean (true if ok)
# --
P(1)=>host
	println("Pinging host " + host)
	Lib:run(List("ping","-c","1",host), List, true) => res
	if (res.exitCode==0) {
		true
	} else {
		println("FAILED with exitCode=" + res.exitCode)
		Inner {
			res.stdout->line println(line) |
			res.stderr->line println("#ERR# " + line) |
		}
		false
	}
/ping
```

After testing the function, we go on to create the function that manages the processes.

### Management function, with logging via Db2Obj database script


We now create a function Hosts, which returns a list of the hosts to check, and then
the function CheckPing, which iterates over these.

```
"s01.s s02.s s03.s s04.s s05.s s06.s s07.s".split
/Hosts

# Delete previous ping stats from database, then run ping on all
# hosts in parallel, collecting results and store in database.
# --
	COLLECTION="stats"  ## Database collection name

	# Clear out results from earlier runs, if any
	# --
	Db2Obj:DeleteObjects(COLLECTION, Lambda{P(1).value.op=="ping"})

	# Iterate over hosts
	# --
	Hosts->host
		# Start individual processes, each calling the ping function with a host
		# --
		data=Dict.set("host",host)
		proc=SpawnProcess(data,ping(host))
		out(proc)
	| _->proc

		# Wait for each process in turn, and collect results
		# --
		proc.wait
		dbObj=Dict
			.set("op","ping")
			.set("host", proc.data.host)  # the data dict from SpawnProcess
			.set("ok",proc.exitValue)
		if (proc.exitValue==false) {
			# failed
			dbObj.set("output", proc.output)
		}

		# Log everything to database
		# --
		Db2Obj:AddObject(COLLECTION, dbObj)
/CheckPing
```

The CheckPing function iterates over the hosts, and for each calls SpawnProcess, with the
host stored in the context data Dict object. This generates a Process object, which is
is sent on with the out() statement.


After the PIPE we now are working with a list of Process objects, which we iterate over,
and for each, first wait for it finish, then pick values from it, building a dbObj Dict
with relevant information. THe field "op"="ping" is what is used to identify these
data, for the initial call to DbObj:DeleteObjects() where we delete any previous stats,
from earlier runs.

### Checking results (in database)


After this has run, we look at the data in the "stats" collection:

```
Db2Obj:ShowFields("stats",List("host","ok"))
<List>
0: 2020-11-05 22:48:44 | s01.s | true
1: 2020-11-05 22:48:44 | s02.s | true
2: 2020-11-05 22:48:44 | s03.s | true
3: 2020-11-05 22:48:47 | s04.s | false
4: 2020-11-05 22:48:47 | s05.s | true
5: 2020-11-05 22:48:47 | s06.s | true
6: 2020-11-05 22:48:47 | s07.s | false
```

Here we list fields "host" and "ok" from the objects. We see that hosts "s04.s" and "s07.s"
failed. We now check the output log for "s04.s".

```
Db2Obj:FindObjects("stats",Lambda{P(1).value.host=="s04.s"}).first.value.output
<List>
0: Pinging host s04.s
1: FAILED with exitCode=1
2: PING s04.s (10.0.11.41) 56(84) bytes of data.
3: From 10.0.0.84 (10.0.0.84) icmp_seq=1 Destination Host Unreachable
4:
5: --- s04.s ping statistics ---
6: 1 packets transmitted, 0 received, +1 errors, 100% packet loss, time 0ms
7:
```
## Advantages


The run time of this code should be that of the host that takes the longest
time to ping (or fail to ping).


Collecting stdout from the Process means that the code that does the work (like
our ping() function) can just print progress and status data via println(), which makes
the code easy to test separately.


Processes also offer full protection from exceptions of all kinds, as they
are caught and listed in full inside the Process.

## Flow control


In some cases, the number of processes can be huge, and we may need to limit the
number of active processes. This is done via a function in the Util script, that
returns an 
**object** which we use as follows. The names "Lxxx" are used to
help indicate that they contain lambdas (though strictly they are closures).

```
# -- Create monitor, decide max parallel processes
mon=Util:ProcessMonitor
limit=4
someData->data

	# About to spawn new process using data
	mon.Lwait (limit)
	proc=SpawnProcess(SymDict(data), ...)
	mon.Ladd(proc)
|

# Wait for all processes to complete
mon.Lwait

# Inspect result from the processes
mon.list->process
	...
|
```

The mon.Lwait lambda waits until number of running processes comes below the limit,
before returning.


See separate sections on closures and objects.

# Calling Java


CFT lets us interface Java code via the Lib.Java object. It contains functions
for identifying classes. We then look up a constructor and call it, getting a
JavaObject in return. We can also look up methods from the class object, and call
them with parameters.


Currently, for this to work, the Java code must exist in the classpath.


Example (also available in script Tests01 as function Test17):

```
Lib.Java.forName("java.lang.String") => String
String.getConstructor(String).call(Lib.Java.String("test")) => obj
String.getConstructor(String).call(Lib.Java.String("123")) => obj2
Lib.Java.Object(obj2) => paramObj
String.getMethod("concat",String).call(obj,paramObj).value
/t17
```

This function looks up the String class, then creates two instances via
the constructor that takes a String parameter. CFT strings values are converted to Java values
via the Lib.Java.String() function.


Then we wrap obj2, which is a CFT value (of type JavaObject), as a Java value,
via Lib.Java.Object(), and locate the concat() method of the String class.


It is invoked on
obj, with obj2 as parameter. The method call returns a JavaValue object,
which has a function value() that returns a CFT value, in this case the
concatenated string "test123".

# String .esc() and .unEsc()


As was mentioned initially, CFT doesn't use backslash as an escape character.
However, we still require a way of converting "difficult" strings to code,
via synthesis. For this purpose, the two functions String.esc() and String.unEsc() was
created.


One rarely needs to call these manually, but they are worth mentioning, as sometimes synthesis
of a string may result in code such as this:

```
"^q^aa^a^q".unEsc
```
## Escape codes


For an escaped string, the escape character is the ^ symbol.



- "Double quotes" ^q


- 'Single quotes' / Apostrophe ^a


- Newline ^n


- Carriage Return ^r


- Tab ^t


- The ^ symbol ^^



This gives a way of creating strings with newlines inside.

```
"this^nis^na test".unEsc
<String>
this
is
a test
```
### Note: CRLF / LF with text files ...


Note that when creating a text file using global function File(...) or via Dir.file(...),
the default end-of-line mark is used (CRLF for windows, LF for Linux). To overrule this,
the File object has two functions:

```
someFile.setWriteCRLF
someFile.setWriteLF
```
# Automating interactive functions / Sys.stdin()


Functions may query the user with Input("prompt").get and readLine("prompt"). If we want
to automate such functions, we use function Sys.stdin() to buffer up any number of
input lines.

```
Sys.stdin("read-this") Input("Enter data").get
<String>
read-this
```

Note that both Input.get() and readLine() detect if there is buffered input, and
if so, do not display the prompt or other info. Particularly useful for Input.get(),
since buffering the empty string "" with Sys.stdin() means repeating the last value.

## Running colon commands from script code


Using the Sys.stdin() statement without being followed by Input.get() or readLine(), is just
another way of entering commands. This means colon commands are available from CFT code.

```
stdin("2+3")
<int>
5
```

This can be exploited to let a script modify itself, by redefining
functions, although that will be troublesome if those functions read input. A better
use is that of running colon commands, particularly loading scripts. This is used
frequently with shortcuts.

```
stdin(":load SomeScript","?")
```
# Clone any value


The function Sys.clone() returns a copy of any value, as long as it is
synthesizable. If not, an error is returned.

```
a=List(1,2,3)
b=Sys.clone(a).add(4)  # "b" contains 1,2,3,4 while "a" remains unchanged
```

The clone can also be done manually:

```
a=List(1,2,3)
b=eval(syn(a))
```
# CFT.props - mCat, mEdit and mMore lambdas


The configuration fields mCat, mEdit and mMore ("m" for macro) define lambdas
that are called for interactive commands cat/edit/more. This means it is possible
to redefine what edit means. Currently, mEdit calls either "Lib:e". The
mMore lambda calls "Lib:m", while the mCat macro just calls .read on file parameter.

# CFT.props - shortcuts


The CFT.props text is self explanatory.

```
# Shortcuts
#
# The shortcuts are lines of code. If that code results in a macro, it is invoked with
# no parameters. Since we don't need parameters, there is really no need for macros
# here. The return value from a shortcut becomes the "last value", available
# via Sys.lastResult, as well as used by :syn etc
#
# Note that shortcuts only work when the prefix is at the start of the interactive
# input line.
# ---
shortcutPrefix = @
shortcut:r = Sys.stdin(":load Release","?")
shortcut:p = Sys.stdin(":load Projects","?")
# List available shortcuts when typing '@' only
# ---
shortcut: = File("CFT.props").read-gt;line assert(line.contains("shortcut:")) out(line)
```

This means that typing @r loads the Release script, then executes the '?' command, which
lists its content.

### Show all shortcuts


To list defined shortcut, just type

```
@
```

This is a shortcut itself, that traverses the CFT.props file and identifies and
displays the
shortcut definitions from it.

# Lib.Text.Lexer


The Lib.Text.Lexer objects adds
capability to match complex tokens with CFT, using the same Java tokenizer implementation
parsing CFT script code.

## Concept


The concept is that of a graph of nodes, each being a map for single characters to other
nodes.


If our parse process so far has led us to node A, and it contains mappings for digits 0-9
pointing at node B, and next input character is a digit, then that character is "consumed" from
input, and we move to node B as new current node.


The process repeats, until end of input, or current node has no mapping for the next character.


At this point, one of the following takes place:



- If the current map is marked with "this is a token of type X", then parsing succeeds


- Otherwise, we backtrack, unconsuming previourly consumed characters, until finding a
map that "is a token of type Y", which is processed as the first case

<lI>
Or, if no map in our parse tree has the "this is a token" mark set, then parsing fails



Following a successful token match, the matching state is reset to the root node of the
graph, working with the next input character. This repeats until either all characters
have been consumed, or we find a character that we can not match, which is usually an error.

## Implementation


In the CFT functions, nodes are created via the Lib.Text.Lexer.Node function.

```
Lib.Text.Lexer help
# Node(firstChars?) - create empty node, possibly identifying firstChars list
# addLine(line) - processes line, adds to internal token list - returns self
# getTokenStream(rootNode) - get TokenStream object
# getTokens(rootNode) - get list of tokens
```

The nodes in turn contain the following:

```
Lib.Text.Lexer.Node help
# addToken(token) - create mappings for token string, returns resulting Node
# addTokenComplex(token, charMapDict) - create mappings for complex string, returns resulting Node
# setDefault(targetNode?) - map all non-specified characters to node, returns target node
# setIsToken(tokenType?) - tokenType is an int, which defaults to 0 - returns self
# sub(chars, targetNode) or sub(chars) or sub(targetNode) - add mapping, returns target Node
```

A simple example:

```
top=Lib.Text.Lexer.Node
x=top.sub("0123456789")   # new node
x.sub("0123456789",x)  # x points to itself for digits
x.setIsToken(1) # token type: non-negative numbers for regular tokens
top.match("123xx")  # returns 3 (length of token), indicating match on sequence '300'
```
### .sub()


The sub() function of any node is used to connect pointers from one map to another. It takes
three forms:

```
(1) someNode.sub("abc",someOtherNode)
# when at someNode and one of the characters ("abc") are next character in input
# string, then consume character, and move to that other node, which may of course
# be "someNode" (back-pointer) or some different node

(2) someNode.sub("abc")
# When no node parameter, an empty node is created, which "abc" points to from
# someNode. The new node is returned.

(3) someNode.sub(someOtherNode)
# When creating libraries of reusable nodes, they always must define a set of
# characters which are called "firstChars". These are the characters that indicate
# the start of some sort of data. For example for Lib.Text.Lexer.Identifier, the
# "firstChars" are "a-zA-Z_". It's the letters an identifier can start
# with. Similarly we can create our own library node functions, by supplying a
# firstChars list as parameter to Lib.Text.Lexer.Node
#
# So what happens is that inside someNode, pointers are added to someOtherNode for
# all characters in that node's firstChars.
```
## Reusable nodes - integer sequence


To create a reusable node, we need to specify the "firstChars" of a node, which are given
as parameter when creating an Node node. This means adding it as "sub" under some other node,
lets those characters point at it.

```
# Create reusable node for integers.
# --
	"0123456789"=>digits
	Lib.Text.Lexer.Node(digits) =>x
	x.sub(digits,x)
	x.setIsToken(1)
	x
/NodeInt

# Now we can for example match a IP v4 address
# --
	Lib.Text.Lexer.Node =>top
	a=NodeInt
	b=NodeInt
	c=NodeInt
	d=NodeInt
	top.sub(a)
	a.sub(".").sub(b) # creates intermediary nodes for the dots
	b.sub(".").sub(c)
	c.sub(".").sub(d)
	d.setIsToken(2)
	top
/MatchIPAddress

# Test
# --
	str="192.168.1.1 xxx" 
	MatchIPAddress.match(str)
	# should return 11 (character count)
/t1
```
## Processing single lines


To process all text in a line, we need to build a root node to which we add
pointers to sub-nodes for all valid tokens. For simplicity, let us match only
identifiers.


If identifiers are separated by space, we also need to match
whitespace. Since we are not interested in whitespace, we assign whitespace
tokens a negative token type, as those get automatically ignored.

```
# Identifiers
# --
	"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_" =>firstChars
	firstChars+"0123456789" =>innerChars
	ident = Lib.Text.Lexer.Node(firstChars)
	ident.sub(innerChars,ident)
	ident.setIsToken(1)
	ident
/Identifiers

# Whitespace
# --
	" ^t^n^r".unEsc =>chars
	Lib.Text.Lexer.Node(chars) =>ws
	ws.sub(chars,ws)
	ws.setIsToken(-1)
	ws
/WhiteSpace

# Root node
# --
	Lib.Text.Lexer.Node =>root
	root.sub(Identifiers)
	root.sub(WhiteSpace)
/Root
```

With the Root node ready, we can now parse strings consisting of identifiers and space,
and since space is assigned a negative token type, it gets automatically ignored.

```
# Test
#
# Using getTokens(), with the Root node as parameter, which returns
# a list of token objects.
# --
	Lib.Text.Lexer.addLine("this is a test").getTokens(Root)->
	token
	report(token.sourceLocation, token.str, token.tokenType)
/test
```

Now running the test we get

```
test
<List>
0: pos=1  | this | 1
1: pos=6  | is   | 1
2: pos=9  | a    | 1
3: pos=11 | test | 1
```

The spaces are nicely consumed and ignored.

## Limitations


As there is at most one pointer per character in each node, we can not both recognize
identifiers AND certain keywords, such as "begin" and "end", separately, since "b" can only
point to one Node, not two.


However, we can easily match symbols that extend other symbols, such as "=" and "==", because
the second is an extension of the first. The matching algorithm described above means
matching as much input as possible, before possibly backtracking to find a token type.

## Different uses


Parsing a programming language or JSON structure, requires us to identify every
token in the string. The lexer tree must know all, and use a top Node that recognizes
the start of each.


Parsing a log line piece by piece does not have this requirement. Different
lexer nodes may be used for each part of the line, some allowing for alternatives,
most just looking to match a fixed format string, for which regular expressions
would also be suited.

## Complex tokens


For the case where we want to identify parts of a log line, one token at a time,
individual token definitions may not co-exist under a shared root, but that is
exactly the point: we have clear expectations for what we look for, at any time,
and can employ different Node roots as we progress.

### Regular Node.addToken() example


With the normal .addToken() function, we can do something like this:

```
Lib.Text.Node =>grade
"A AA AAA B C".split->x 
	grade.addToken(x).setIsToken
|
```

Overlapping definitions, such as "A" and "AA" and "AAA" is not a problem for Node.addToken()

### Node.addTokenComplex() example


This function adds a token, where some of the characters in the token string map to
sets of characters, via a Dict object. This function does not have the freedom to
expand and reuse existing (overlapping) nodes, as with the regular .addToken() function.


It is meant for matching one thing only, and not for collecting all token definitions
under a shared root, as before.

```
Lib.Text.Node =>date
Dict.set("i","0123456789") =>mappings
date.setTokenComplex("iiii-ii-ii", mappings).setIsToken
date.match("2020-09-15xxx")  # returns 10 (characters matched)
date.match("2020-009-15xxx") # returns 0 (no match)
```

Feels like Regex character classes, no?

# ANSI escape codes


The Curses script contains code for producing ANSI escape sequences to set text color,
bold and underline, as well as clearing the screen, and moving the cursor, which
enables drawing boxes, for example.


Since curses may not be supported on every device, the ANSI support is disabled
by default, but can be enabled
via a call to

```
Curses:Enable(true)
```
This changes all the functions inside Curses from returning empty strings, to return ANSI escape sequences.

# Passwords, encryption, binary data

## Passwords


To read a password (no echo), call Sys.readPassword function.

### Encrypt / decrypt


The Lib.Util object contains two functions, encrypt() and decrypt(), which both
take a password string and a salt string. These together form a complete
password, but the salt is not necessarily secret, just a way of differently initiate
the encryption engine with the same (secret) password.

```
"password".getBytes("UTF-8")  # returns Binary object
/password
Lib.Util.Encrypt(password).processString("this is a message")
/x
Lib.Util.Decrypt(password).processString(x)
<String>
this is a message
```
### Binary data


The processString() function of the Encrypt and Decrypt objects, is nice when
storing encrypted strings in a database that only handles strings, such as Db2.


The more generic function process() operates on binary data, and lets us
save encrypted versions of files.

```
File("x.txt").readBinary             # returns Binary
"some string".getBytes("UTF-8")      # returns Binary
File("x.txt").binaryCreate(binary)
<someBinary>.toString("UTF-8")
```
### Session data


In addition to the public Sys.getSessionUUID, there is a secure value returned by
Sys.getSecureSessionID(). It is a secure Binary object, which means it has no
functions, and can only be passed as parameter to system functions, like encryption.


The Vault script uses this to persist session secrets, in a secure way, stored
in Db2 encrypted with the secure session id. By encrypting a second static value,
it detects whenever there is a new session, prompting the user to enter the
the secret values, to store for that session.

# Reference: object types

**Per v3.4.4**

```
Grep("extends Obj") => g
Sys.homeDir.allFiles(Glob("*.java"))->f
	g.file(f)-> line
		out(line.after("class").before("extends"))
| _.sort->x
println(x)
/objects

DD
DDBrush
DDD
DDDBezier
DDDBrush
DDDRef
DDDTriangle
DDDVector
DDDWorld
DDLineBrush
DDRef
DDVector
DDWorld
ObjAnnotatedValue
ObjCIFS
ObjCIFSContext
ObjCIFSFile
ObjClosure
ObjColor
ObjConvert
ObjData
ObjDataFile
ObjDate
ObjDateSort
ObjDb
ObjDb2
ObjDict
ObjDir
ObjDuration
ObjEncrypt
ObjExtProcess
ObjFile
ObjFiles
ObjFilter
ObjFilterReader
ObjGlob
ObjGlobal
ObjGrep
ObjInput
ObjJava
ObjJavaClass
ObjJavaConstructor
ObjJavaMethod
ObjJavaObject
ObjJavaValue
ObjJavaValueBoolean
ObjJavaValueInt
ObjJavaValueLong
ObjJavaValueNull
ObjJavaValueObject
ObjJavaValueString
ObjJobs
ObjLexer
ObjLexerNode
ObjLexerToken
ObjLexerTokenStream
ObjLib
ObjLineReader
ObjMath
ObjPersistent
ObjPlot
ObjProcess
ObjRegex
ObjSys
ObjTerm
ObjText
ObjUtil
ObjWeb
ObjWebRequest
ObjWebServer
ObjWebServerContext
Value
```
# Reference: Value types


**Per v3.4.4**
```
Grep("extends Value") => g
Sys.homeDir.allFiles(Glob("*.java"))->f
	g.file(f)->line
		out(line.after("class").before("extends"))
| _.sort->
x
println(x)


/values
ValueBinary
ValueBlock
ValueBoolean
ValueFloat
ValueInt
ValueList
ValueNull
ValueObj
ValueObjFileLine
ValueObjFloat
ValueObjInt
ValueObjStr
ValueString
```
# Reference: Expressions vs statements


Almost all code in CFT are expressions. Function calls are of course expressions, and so are assignments.


Even blocks, both local and Inner, are expressions.


Instead of describing all expressions, it is easier to list the statements.

## Statements


These are the statements in CFT:



- Looping and iteration over lists


- assert/reject/break


- out(), condOut() and report()


- the addDebug() command


- the help command


- interactive commands "cat", "edit", "ls", "cd"


- the "showCode" command



As of version 2.8.2 there are two global functions that list out
details about built-in syntax for expressions and statements:

```
_Stmt
_Expr
```

These are listed at the top when typing help to show global functions.

# Reference: Colon commands


Colon commands are best described by entering a single colon at the CFT prompt.

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

Confusing colon commands with shortcuts?


Colon commands exist outside the language, and are fixed (written in Java), while shortcuts run CFT program
code, and are defined in the CFT.props file. So far all good.


The "problem" is that CFT code (and so shortcuts) can run colon commands via "abusing" the Sys.stdin() command.

# Reference: Synthesizable types



- boolean
- int
- float
- string
- null
- AValue
- Date
- Date.Duration
- Dict
- Dir
- File
- FileLine
- Float
- Glob
- Int
- List
- Regex
- Str


# Comments and digressions

## Function name AFTER code?

```
Dir.files
/showFiles
```

This stems back to the time of entering code line by line. Having to decide the name of a function before
seeing how much functionality you got crammed into one line, or if it at all worked,
made little sense. Instead you write some code
that does something useful, then decides what to call it.


An "advantage" of this is that functions don't need to be parsed when loading a script. Functions are
defined as all lines of text since top of file or since last "/xxx" line. Now, searching for the first
non-blank line of a function, to present in the "?" list of functions, and letting that be a comment,
makes sense.

While pondering how to integrate classes as top-level elements along with functions, I decided
to keep the /something notation, because it lets the code dealing with both loading and saving
source files deal with lines of text, not parsing.


The syntax with the slash and an identifier was inspired by PostScript.


I also am very pleased with parameter handling in CFT functions, so even if we were to move to
a more mainstream notation, I would want to keep the P() function and now also the "as" type
checks.

## Using Sys.stdin to run colon commands etc


This functionality is an example of an "unexpected feature", as the Sys.stdin() was created to automate
functions that used
Input and readLine(). There even was a moment of confusion when discovering what happened to input lines not consumed
by those interactive functions.

## Code spaces / the "pipe"


This stems from the "one-line-at-a-time" period, where scripts were entered from
the command line, at a time long before introducing block expressions. Being a fairly compact
and efficient notation, and frequently used, code spaces and the "pipe" symbol will
remain in the language.


The nice thing with the PIPE is that it is global within the function body, but this
may also be a weakness when one is used to nesting stuff with braces, like in Java.

## Code spaces vs Inner blocks


Code spaces were invented long before any blocks. Splitting the function body into
spaces with the PIPE is enough for most tasks.


The need for code blocks only really arised after the "if" was added. The first implementation
shared the "local" block syntax, but functionally worked like an "Inner" block.


It took a while back and forth deciding we needed two different non-lambda block expressions,
and defining them in terms of code spaces.


The Inner blocks should possibly have a better name. I have considered many, but not
decided on one that gives a more intuitive feel.

```
# Any of these?
do {...}
Compute {...}
sub {...}
```


## Script and code size

### 2020-11-13 v2.0.0

```
Script code:      ~5k lines
Java code:        ~20k lines
Functions:        ~290
Object types:     ~45 (including Value types)
```
### 2021-04-01

```
Script code:      ~8k lines
Java code:        ~21k lines
Functions:        317
Object types:     36
Value types:      12
```
### 2021-09-26


Created CodeStats script:

```
Script code:      10922 lines
Java code:        23907 lines
Functions:        377
Object types:     57
Value types:      13
```
### 2021-10-16

Running CodeStats:main

```
Script code:      11488 lines
Java code:        24644 lines
Functions:        378
Object types:     57
Value types:      13
```
### 2021-12-17 v3.0.0


Running CodeStats:main

```
Script code:      12751 lines
Java code:        30244 lines
Functions:        483
Object types:     69
Value types:      13
```
### 2022-03-31 v3.2.5


Running CodeStats:main

```
Script code:      15695 lines
Java code:        32022 lines
Functions:        490
Object types:     69
Value types:      13
```
### 2022-04-22 v3.5.0


Running CodeStats:main

```
Script code:      16091 lines
Java code:        33120 lines
Functions:        507
Object types:     72
Value types:      13
```

### 2022-12-11 v3.5.6

Running CodeStats:main

```
Script code:      17031 lines
Java code:        34123 lines
Functions:        510
Object types:     72
Value types:      13
```



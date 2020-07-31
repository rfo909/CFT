# CFT / ConfigTool

```
Last updated: 2020-07-31 RFO
v1.1.2
```
# Introduction


CFT is a programmers tool, in the form of a Java application that runs in a
terminal window. It has no graphical
elements, which means it can run everywhere. From the start it has been developed
as an interactive environment, with strong emphasis on automation. The syntax is compact,
in the interest of getting useful work out of even a single line of code.


CFT is tested on Linux and Windows, and easily integrates with external commands
on both, such as PowerShell, git, ssh. It should run everywhere that supports Java.


Development has been going on since May 2018, and v1.0 towards end of June 2020
marked a certain level of robustness and error
handling, being mature enough for open source release on github.


It started out purely as an interactive
tool, with emphasis on compact syntax, since all code was entered via the command line. After
a while it evolved into editing the savefiles, or "script files".


Functionality has been driven partially by needs and partially by what's fun to implement.

# What CFT is not


CFT does not allow classes or objects apart from those predefined. That would
increase complexity a lot, be harder to implement (scope rules, inheritance etc),
and is not what was needed.


CFT is meant for automation tasks, such as moving files, searching and running
external programs. It is a Domain Specific Language, not general purpose.


Writing big functions is certainly possible. Letting scripts call each other is
also both supported and used. But writing complex applications, or introducing
abstractions, is not the intention
behind this application.


**It is a do-this-then-that type of language.**

Also, for many purposes, we will call external programs. In this respect, CFT is a shell, in that it
depends on external programs. Still, CFT is not a full shell. You will usually also need regular shells
for all kinds of interactive commands, as well as file managers etc.


CFT doesn't support autocomplete, since its interactivity is line based.

# So what is it for?


Commands like "ls" and "cd" exist, with globbing, as well as "more" and "edit" (which opens a file
in an editor), and they are meant for moving around the directory tree. Actually changing
and moving files and directories is supposed to be scripted with code.


Everything in CFT is functions, and defining your own is super easy, and 
**this is the point of CFT**,
the automation aspect, and that automation should be a simple and interactive process.

## Functions example


CFT is object oriented, as opposed to traditional *ix shells such as bash, which process text.


This means that the return value from some function always is an object that has other functions
inside. Strings, integers and floats are all objects.


Example:

```
$ Dir.files.length
# means:
# - call global function Dir() with no parameters
# - returns Dir object for current directory
# - call .files() inside the Dir object
# - returns list of File objects
# - call .length() function on the list object
# - returns an int
```
### No parantheses?


When calling a function without parameters, the parantheses () are optional.

## Coding vs purely interactive use


Working on mission-critical systems, one must at all costs avoid issuing the wrong commands.
Creating functions, and preferrably adding code to validate input, to reject specific server
names or domains, etc, plus the .protect() mechanism for Dir and File objects, helps you avoid doing
seriously bad things.

Better use a little time to step-wise develop some function with proper checking of
parameters from users or other functions, before you let it actually do anything, rather
than interactively mistype, and kill the wrong database or VM.


Even boring stuff has the potential for serious consequences, if you delete the original log
files, instead of the copy you unzipped. Use the protect() mechanism routinely when pointing
a Dir at a location where destructive updates must not happen, and save yourself from
some of those "just need to do this in a hurry" typing mishaps.

## Typical use



-  copying files


-  searching source code and logs


-  deploying jar files to test environments


-  restart services


-  delete logs older than X days - carefully


-  verify that a bunch of VM's can be pinged


-  generate netplan config files


-  automated management via ssh


-  automate git and powershell and other things that require you to type too much


# CFT as a shell

## Navigating directories


CFT started out as an interactive shell-like app. It runs in a terminal window, and offers a simple '$' prompt, where one enters commands.

```
$ ls
$ cd subdir
$ cd ..
$ pwd
$ ls ../someDir/*.txt
```
## Show content of file


Now if we want to list content of file "TODO.txt", we can enter

```
$ cat TODO.txt
$ more TODO.txt
$ edit TODO.txt
```
# The help system


To get information about what global functions are available, type "help" and press Enter. A list of global
functions is displayed with a short description for each. To get help for functions available inside an
object, put an object of that type on the stack followed by help.

```
$ help
$ "" help
$ Dir help
$ 3 help
$ 0.1 help
```

The first produces a list of global functions, the second of functions inside string objects, and
the third displayes the functions inside Dir objects. The fourth lists functions available on
int, and the last for float values.

# Lists


Lists are created with the global List() function, which creates
a list from all its parameters.

```
$ List(1,2,3,4)
```

Lists are also returned from many other functions, for example
calling Dir.files() function produces a list of File objects inside that
directory object.

```
$ Dir.files
```

Many functions are available on a List object. One frequently used is "nth", which
gets a specific element, defaulting to 0 if no argument.

```
$ List("a","b","c").nth
<String>
a
```

For details of available functions, as always use the help system:

```
$ List help
```
# Files

```
$ File("x.txt")
<obj: File>
x.txt   DOES-NOT-EXIST
```

The File() function requires a name, and returns a File object. As seen
above, the file needs not exist.


File objects created with a simple file name (no path), are always located in
the CFT home directory. This gives predictability for certain data files etc.


To access or create files in other directories, enter an absolute or relative
path in the parameter to File(), or use the file() function inside
some Dir object:

```
$ SomeDirExpression.file("x.txt")
```
## Create file


To create a file with a single text line:

```
File("x.txt").create("one line")
```

If the file already exists, it is overwritten with the new content.


Usually, we create files with more than one line, and this is done supplying a List
instead of a single value, as parameter to the File.create() function.

## Read file


To read an existing text file:

```
File("x.txt").read
```

This returns a list of all lines in the file.

## Append to file


To append a single line to a file:

```
File("x.txt").append("another line")
```

To append multiple lines, append a list instead.

## Page through a file


To page through text file

```
more x.txt
```
## Show bytes of file


To page through list of bytes (hex)

```
File("x.txt").hex
```
## Encoding


Default encoding is "ISO_8859_1", but this can be changed, for example:

```
File("x.txt").encoding("UTF-8")
```
## Delete a file

```
File("x.txt").delete
```
# Directories

```
$ Dir
<obj: Dir>
ConfigTool/ d:5 f:20
```

Calling the Dir function with no parameters returns a Dir object for the current directory. The Dir
object offers multiple member functions, one of which is 
**.files()**, which produces a list of files in
the directory. We can also call the Dir function with a path parameter.

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
## Delete a sub-directory


The sub-directory must be empty

```
Dir.sub("something").delete
```
# The shell() function


The global shell() function starts a shell inside CFT. When you exit from it, you're back
in CFT.

```
$ shell
(starts bash or cmd or Powershell or something else)
exit
# Running /usr/bin/bash completed: 25529ms
$
```
# Core types



- String


- int - (Java long)


- float - (Java double)


- boolean


- List



All values in CFT are objects, which may contain functions. Strings can be written using double
or single quotes.

## String literals


Strings are written in single or double quotes, and can be summed with '+', which allows
for all kinds of combinations.

```
$ "'a'"
<String>
'a'
$ '"' + "'a'" + '"'
<String>
"'a'"
```

Also, backslash is not used as escape character, which means backslash is just another character,
simplifying those Windows paths.

# List iteration / filtering


Lists are essential for all processing with CFT.


Lists can be created manually using the global List() function.

```
$ List(1,2,3)
$ List("a","b","c")
```

A much used way for creating lists of strings, is to use the string function split(), which by default
splits a string on spaces. This means the following produce the same result.

```
$ List("a","b","c")
$ "a b c".split
```
## Iterating over list content


The iterator in CFT takes the form of an arrow followed by a loop variable. For a loop construct
to return output, we use the out() statement inside.

```
$ "1 2 3".split->x out("a"+x)
<List>
0: a1
1: a2
2: a3
```

The result is a list of strings, as displayed.

## Filtering with assert(), reject() and break() + out()


Using the assert() statement, we may abort processing for elements that do not meet a condition.

```
$ Dir.allFiles->f assert(f.name.endsWith(".java")) out(f)
```

The reject() statement is the inverse of assert(), and aborts processing for elements that meet
a certain condition.

```
$ List(1,2,3,2,1)->x reject(x>2) out(x)
<List>
1
2
2
1
```

The break() statement terminates the loop if the condition is true.

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
$ List(1,2,3,2,1)->
x condOut(x
<2,"(") out("b") condOut(x
<2,")") | _.concat
<String>
(b)bbb(b)
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
# Creating functions

## Naming lines of code


When some code does what we want, we typically create a function from it. This is done by entering

```
... (some code) ...
/name
```

This assigns the name "name" to the last line, and we have now created a custom function.

```
$ Dir.allFiles-> f assert(f.name.endsWith(".java")) out(f)
$ /JavaFiles
```
## Calling a function


To call the above function, just enter its name.

```
JavaFiles
```
## Listing custom functions


Enter '?' and press Enter.

```
$ ?
+-----------------------------------------------------
| JavaFiles: Dir.allFiles->f assert(f.name.endsWith(".java")) out(f)
+-----------------------------------------------------
| .        : Dir.allFiles->f assert(f.name.endsWith(".java")) out(f)
+-----------------------------------------------------
```

Here we see function 'JavaFiles' and its code. We also see the last entered code line, with
a dot '.' in front of it. The last code line is the one that can be given a name, and further,
the last code line can be repeated by entering a single dot and pressing enter.

# Savefiles

## Save


To save all named functions, enter the special command below

```
$ :save Test
```

This creates a file under the CFT home directory,
called savefileTest.txt.

## Load

```
$ :load Test
```
## Create new empty script


To create a new script from scratch, there is the colon command:

```
$ :new
```

## The @e shortcut


A common shortcut is @e, which opens current savefile in an editor:

```
$ @e
```

Shortcuts can be redefined in the CFT.props file.

## CFT.props - codeDirs


The CFT.props file contains the following line by default

```
codeDirs = . ; code.examples ; code.lib
```

The codeDirs field defines a search order when loading scripts.
The first directory is 
**always** used when you type ":save".


The code.examples contains some example code for various use, while code.lib contains
library code, used by most other scripts.


This means you are free to save a script using the name "Lib", and it will be written to
the code.work directory. Doing this means it will hide the version in the code.lib directory.
Which may be perfectly fine, as long as it is what you intended.

# Protecting files and directories


To save typing, one often create functions that just return some directory, or
some files.  The JavaFiles example above illustrates this.


The protect mechanism in CFT lets us attach a protect state to any Dir and File object,
which guarantees that:



- all files and directories derived from it are also protected


- blocks destructive modifications


## Example


Adding .protect to each file that the JavaFiles function generates, ensures that all
files returned from this function are blocked against accidental delete and modifications.

```
$ Dir.allFiles-> f assert(f.name.endsWith(".java")) out(f.protect)
$ /JavaFiles
```

Demonstration:

```
$ JavaFiles.nth.append("")   # Trying to append empty line to first file
ERROR: [input:16] INVALID-OP append : /home/roar/Prosjekter/Java/CFT/src/rf/configtool/main/SourceException.java (PROTECTED: -) (java.lang.Exception)
```

The .protect() function can also take a description string, which if present, is displayed in this error.

## No guarantee


Calling .protect on a Dir object, before using it to locate files, will propagate the protected
state to all those files. However, creating a new Dir object for the same path, without calling
.protect() on it, and then accessing content via this, does not protect anything.


Note also that .protect can not detect for example using the path of a protected File object in
an external program, or even to create a new File object (which will not be protected). Example:

```
File(protectedFile.path)
```
## Laziness is our friend


Usually we will reuse an existing function over creating a new one that does the same.


It may be good design to create functions that return top directories, and protect those
where we don't want to introduce changes, with secondary functions for locating sub-content,
depending on the first function to produce the start point.

# CFT as a functional programming language

## Code only


A major point of CFT is that when we define names using "/name", these names are functions, which
mean they point to code. When entering code, it is immediately executed, leaving us with a result. When we are happy with the outcome, we may assign a name to the function.


The point is that it is the function code that is named, not the result.

```
$ "1 2 3".split
<List>
0: 1
1: 2
2: 3
$ /data
```

Entering the "/data" command
assigns a name to the last code line, not the data it returned. Every reference to the
custom function "data" will now run the code over again, and produce (the same) result.


Entering '?' to list your defined functions, you see this clearly.

```
$ ?
+-----------------------------------------------------
| data: "1 2 3".split
+-----------------------------------------------------
| .   : "1 2 3".split
+-----------------------------------------------------
```
## Local variables


The inside of a function may use local variables for simplifying expressions etc. Variable
assignment is "stack based", in that it is the current value from the stack that is assigned
to the local variable.

```
$ 3 =a 2 =b a+b
<int>
5
```
## Stack vs expressions


Even though CFT uses a data stack, expressions are not stack-based.


This is because  writing postfix expressions is too bothersome (ex. "3 2 +"), so CFT
only understands expressions using regular infix notation ("3+2"). Respecting normal cardinality rules,
we have that 2+3*5 becomes 17.

## Nested loops


Loops are implemented using the "for each" functionality of "-> var". Loops may well be nested.

```
$ List(1,2,3)->x List(1,2,3)->y  out(x*y)
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

## Loop spaces - "pipes"


The body of any loop is the rest of the code, or until a "pipe" symbol
is found. The pipe symbol ("|") more accurately breaks the code into a sequence of
**loop spaces**, which means acting as an end-point for
running loops.


The way a "pipe" works, is to wait for all current loops to terminate, then take the
return value from that loop space and putting it onto the stack for the next loop
space to work with (or do something else). Example:

```
$ Dir.files->f out(f.length) | =sizes sizes.sum
```

This single line of code first contains a loop, which outputs a list of integers for
the sizes of all files in the current directory. Then the "pipe" symbol terminates that
loop space, and creates a new one, where we pick the result from the previous loop
space off the stack and assigns it to a local variable. We then apply the sum() function to it.


To save us some typing, the special expression "_" (underscore) pops the topmost value off
the stack.

```
$ Dir.files->f out(f.length) | _.sum
```

As we see from the above code, a loop spaces don't 
**need** containing loops. The
following is perfectly legal, although a little silly.

```
$ 2+3 | | | | =x x | =y y | _ |
```

Yes, it returns 5.

## Result value from a loop space


All bodies of code in CFT consist of one or more 
**loop spaces**. The result value
from any such body is the return value from the last loop space.

### If the loop space contains looping ...


If a loop space contains loop statements, the result value is a list of data generated
via calls to out() or report() statements. If no actual iteraions take place, or
filtering with assert(), reject() or break() means no data is generated via out() or report(),
then the result is an empty list.

### Otherwise ...


A loop space that doesn't contain loop statements, has as its return value the topmost
element on the stack after all code has executed. If there is no value on the stack,
the return value is 
**null**.

## Function parameters


Custom functions can also take parameters. This is done using the P() expression, which
identifies the parameter by position. Note that 
**parameter position is 1-based**.

```
$ P(1)=a P(2)=b a+b   # fails!
```

If you enter the above code interactively, it will fail, complaining that you can not add
"null + null". This is because there is no call yet, and so there are no parameter values. This is
fixed by letting the P() expression take a second parameters, which is a default value expression.


The default value parameter to P() is important for three reasons.



- Allows the function code to execute while being developed interactively


- May act as documentation in the source


- Provides an elegant way of making functions interactive and non-interactive at the same time,
as the default expression is evaluated only when parameter is not given (or is null),
and may then ask the user to input the value.


### Simple example


Here we make an improved version of the JavaFiles function, that takes a directory parameter, and if none given, uses the current directory ("Dir").

```
$ P(1,Dir) =dir dir.allFiles->f assert(f.name.endsWith(".java")) out(f)
:
: (code result)
:
$ /JavaFiles
```

Typing this code interactively means it gets executed directly, with no actuall call
parameters. This means P(1) is null, and so the default value ("Dir") is used, which means
the code runs on current directory.


After defining the function, we may call it without parameters, or supply null for
a given parameter, which again means the
default expression is used, and the code again executes on current directory.


But now we can let this function run on a custom Dir object, regardless of the current
directory.

```
$ Dir("/home/user/project1")
$ /DirProject1
$ JavaFiles(DirProject1)
```
### Asking for missing value


The default expression of P() can also be used to ask for a value when the parameters
is missing.

```
P(1,Input("Enter value").get) =value ...
```
# Conditional execution


Conditional execution in CFT takes two basic forms.

## List filtering


The assert(), reject() and break() statements inside loops decide if the rest of the loop body is to
be executed or not, and provide the data filtering mechanism in CFT.

## if() expression


The if() statement takes three parameters, the condition, the if-expression, and the
else-expression. Depending on the condition only one of the two following expressions
are resolved.


In this example we call SomeFunction, then use an if() expression to replace return value 'null'
with integer 0 (zero).

```
SomeFunction(...) =a
if(a==null, 0, a) =a  # provides a default value if a is null
```
## when() expression


The when() expression is like an if() expression minus the else-part.

```
if (bool, expr)
```

If the boolean expression resolves to true, then the 'expr' is resolved, and this becomes the return
value. Otherwise, the 'expr' is not resolved, and the return value is null.


The when() expression is good for handling multiple-choice ladders ("switch" in Java).

```
when (mode==1, SomeFunction(...))
when (mode==2, SomeOtherFunction(...))
when (mode==3, ... )
```
## error() expression


The error() expression is another that contains a conditional part, and if true, throws
an exception with the string part, terminating current execution.

```
$ error(1+1==2,"oops")
```
# Block expressions


Block expressions are sequences of code
inside curly braces, and are frequently used with if() and when().

```
false =ok
when (someCondition, {
println("hello there")
true =ok
})
```
## Block expressions are real expressions


Though mostly used with when() and if(), block expressions are general expressions, just
like a function call, or some variable lookup.

```
{2+3}.bin
<String>
00000101
```
## Pitfalls


Block expressions occur literally inside other code, and have their variable scope
extend out into the calling environment, but they are still somewhat function-like. This means:



- One can not pass data to or from block expressions via the stack, as they operate their
own stack internally


- Loop control does not extend from inside a block expressions to the surrounding code, partially
because block expressions can have their own loops. This means that
for example calling break(true) has no effect on the caller.



Example:

```
"a b c".split->x
when(x=="b", {break(true)})  # does not break loop
```

The call to break() inside a block expression does not affect the loop running outside
the block expression.


With break() taking a boolean value, the above example is a bit artificial, as the solution
here is to write:

```
"a b c".split->x
break(x=="b")
```
## Full functionality


Block expressions can contain looping and the code can be partitioned into a
number of loop spaces (with the "pipe" symbol). Example:

```
List(1,2,3)=a {a->
x assert(x%2==1) out(x) | _.sum}
<int>
4
```
# Running external programs

## Summary


The functions for running external programs are part of the Dir object, implicitly defining
working directory for the program.

```
$ Dir.run ( list|...)
$ Dir.runCapture ( list | ...)
$ Dir.runDetach ( list|...)
$ Dir.runProcess ( stdinFile, stdoutFile, stdErrFile, list|... )
```

The parameters written as "list|..." means either a List object, or a list of
String values, separated by comma.

## Dir.run()


This command is used for running external programs in the foreground. What this means is that if
the program requires user input, we can give it, and the CFT code will not continue until
the external process has terminated.

```
$ Dir.run("cmd","/c","git","pull","origin","master")
```

Many Windows programs require the "cmd","/c" in front of the actual program.
For proper operating systems (Linux) you naturally skip the two first elements of the command list.


Often it is easier to use String.split in this case, as Dir.run() accept a single List value
instead of a list of values.

```
Dir.run("cmd /c git pull origin master".split)
```
## Dir.runCapture()


This works the same as Dir.run(), but returns a List of strings representing stdout from the
external program, to be processed further.

```
Dir.run("which","leafpad") =lines lines.length>0 && lines.nth.contains("leafpad")
/HasLeafpad
```
## Dir.runDetach()


Use to run external program in the background. The CFT code continues running after forking
off the background process. Nice for editors etc.

```
$ Dir.runDetach("leafpad", Sys.savefile.path)
```

This example runs the (linux) leapad editor in the background, with the path of the current savefile as
argument.

## Dir.runProcess()


Similar to Dir.runDetach(), but for cases where we need to provide non-interactive input,
and inspect the output, as this uses files for stdin, stdout and stderr.

# Editing save files

## Using editor for entering code


As was illustrated above, in the case of Dir.runDetach(), we can create a function that opens
the current savefile in an editor. This of course requires a current savefile, which is created
by colon command save. The Sys.savefile() function returns a File object for the savefile,
or null if no savefile exists.


Since this is functionality is used very frequently, there exists a global shortcut that does this.

```
$ @e
```
## Auto reload


After modifying a script in an editor, and saving it, CFT automatically reloads it the next
time you run something.

## No auto save!!


Note that when modifying a script from within CFT, by defining or redefining functions, there
is 
**no auto save**. This is to allow experimentation without making changes permanent,
but requires that you remember to save manually when done.


If you have never saved the script, you must enter a name:

```
$ :save SomeName
```

Having saved the script before, you need only enter the following:

```
$ :save
```
### Losing changes


Doing changes to a script inside CFT, but forgetting to save, then opening the savefile
in an editor, and saving, causes CFT to automatically reload the savefile, losing the initial
changes.


After doing interactive changes inside CFT, always save. Also either refresh file in open
editor, or close editor and open new.

## Multi-line functions


Editing the savefile lets us write more complex functions as we can break function
code across multiple lines, using indentation and comments. The same rules apply as for
single lines, regarding how the "pipe" symbol sections the code into multiple
loop spaces.

## Comments


The hash character '#' indicates that the rest of the line is a comment.


Note that ALL TEXT following one function assigment ("/name") is included as the
code body of the next function.

## First line of a function ...


The interactive command '?' is used to see the defined functions. For functions entered
interactively there is only one line, and that line is shown. For multiline functions,
the first line is displayed.


Letting the first line of a multi-line function be a comment is a great way of
producing meaningful content for the interactive '?' command.


Alternatively, putting all parameter grabs on a single first line, serves as great
documentation of function interface when seen with the '?' command.

## Debug


When creating complex code, we may need to display debug output. This is done with the global
debug() function, or you can use the println() function.

```
$ debug("data")
%DEBUG% data
```

You can of course also just call println(...)

# Interactive use

## Change current directory: cd


The "cd" command should function mostly as expected. CFT has no autocomplete feature, since
it only reads lines, not key-presses.


CFT handles globbing with '*' in the last part of the path, so you can enter:

```
$ cd Proj*
$ cd ../OtherProj*  # will fail if more than one match
```

The "cd" command also has a "funtion mode", where you can enter

```
$ cd (expr)
```

The expression must resolve into a Dir object.

### Change drive letter in Windows


The following is used to switch drive letter in Windows.

```
$ cd d:\
```
### Paths with space


Also note, that paths with space in them need to be put into
quotes.

```
$ cd "c:\Program Files\blah\blah"
```
## List files: ls


The "ls" command comes in three variants:



-  "ls" - lists directories and files


-  "lsd" - lists directories only


-  "lsf" - lists files only



As with "cd", it handles globbing with '*' in the last part of the path:

```
$ ls *.txt
$ ls ../OtherProject/*.xml
```

Also as with "cd", the "ls" command has a "function mode", where it takes an
expression inside ()'s.

```
$ ls (expr)
```

The expr should resolve into a Dir object.

## Display text file: cat, more, edit


The "cat" command lists the content of a file, while "more" pages through the file, and
"edit" opens an editor with the file. All these also understand globbing in the same way
as "cd" and "ls"

```
$ cat README*
$ more README*
$ edit README*
$ cat ../OtherProject/build*.xml   # will fail if more than one match
```

As with "ls" and "cd", these also handle "function mode", where we pass an expression
inside ()'s. Example:

```
$ edit (Sys.savefile)
```
### Editing a previous file again


Each time "edit" is used to display a file in an editor, a history log is maintained. To
display this log, and select a file from it, just type enter with nothing following. This
only works in interactive mode.

```
$ edit
```
### cat, more, edit - implementation


These three commands (statements) are defined via macros in the CFT.props file, which for
"edit" and "more" calls functions inside the Lib script (under code.lib).

<o>
This means that their functionality are not complete defined within the CFT java code, and
that their functionality can be modified without recompiling the code.

## Print working directory: pwd


The command "pwd" is also implemented as an expression, and returns the current directory. This
is the same as the "Dir" expression.

```
$ pwd
```
## Using cd, ls, cat, more and edit inside (function) code


Quick answer: 
**this should be avoided.**

These statements, unless called in "function mode", with ()'s, WILL consume all tokens up to the
end of the function, or the next PIPE. They will mess up your code, and fail. For use on the
interactive command line only.


In script code, there generally is no point in navigating to a current directory. The
"current directory" is really a concept for interactive use. Once you get to a
directory you need to write code for:

```
$ pwd
$ :syn
$ /MyDir
```

The :syn command synthesizes code from the value returned from pwd, and then we assign it
to a function. From then on, using MyDir to access sub-directories or files, gets the
same results independent of the current directory.


To start an editor or page through a file from code, please use the following:

```
$ call "Lib:e" (file)  # open editor
$ call "Lib:m" (file)  # page through file with more
```

This is what the "edit" and "more" commands uses internally anyway.


See CFT.props for the cat, more and edit macros for details.

## Get data from user: Input() and readLine()


To stop and ask the user to enter something, we got two options. The Input() function
produces an Input object which maintains a
session state, remembering earlier values, and suggesting the last value entered as default,
with the ability to show and select earlier values.


You need to call the ".get()" function on the Input object to actually ask the user to enter
data.


The readLine() function just pauses and reads a line. Both Input() and readLine() take a
prompt parameter. For Input objects, the session state remembering previous inputs, is
tied to the prompt string.

```
$ Input("Enter search text").get =text
$ readLine("Enter name") =name
```
# Searching files / report()


To search files, we usually use the Grep object. It takes a list of alternatives to look for,
and can optionally be instructed with patterns to reject. It has a .file(File) function that
runs the search on a file, and returns a list of lines that meet the requirements.


But the lines returned from "Grep.file()" are not
just strings, they are extended strings, of a type called "FileLine", which contains
two additional additional functions, ".file()" to get the
file object and ".lineNumber()" which returns the line number in that file.


The File.read() function also produces a list of FileLine, rather than regular string
values.

## report()


We already know that iterative loops can generate output using out(), but there is a second way,
which is the report() statement. It can take one or more parameters, and results in a list of strings that
are formatted into columns, for readability.


The formatted text is added to the same list of output as data via out(), but only after the
loop space has completed. This means that the formatted output from report() is data (strings),
just like values from out().

## Multi-line example


Below we will show a multi-line example function, which we create editing the savefile,
that asks the user for a search text, then searches
some directory for all source files (java), and look for the pattern in each. The result is presented
in readable format, using report()

```
Dir("/home/user/project1")
/ProjectDir
Input("Enter search term").get =st
Grep(st) =grep
ProjectDir.allFiles->f
assert(f.name.endsWith(".java"))
grep.file(f)->line
report(line.file.name, line.lineNumber, line)
/Search
```

After adding the text to the script file using an editor, save it, then run Search in the
CFT terminal window.

```
$ Search
Enter search term
class
<List>
0: ProgramLine.java       | 11  | public class ProgramLine extends LexicalElement {
1: StmtDebug.java         | 9   | public class StmtDebug extends Stmt {
2: ExprIf.java            | 9   | public class ExprIf extends LexicalElement {
3: LexicalElement.java    | 5   | public class LexicalElement {
:
:
```
## Grep - complex example

```
# Match lines containing "a" or "b", and that do not contain "c" or "d"
Grep.match("a","b").reject("c","d")
# Match lines containing "a" AND "b"
Grep.match("a").match("b")
# Regular expressions - matching lines with 8 digits in sequence
Grep.matchRegex("[0-9]{8}")
```
## Working with huge logs - counting hits


The Grep instances are by default set up with a limit of the 1000 first matches. Passing
that limit produces an error. The limit can be changed as follows:

```
$ Grep("...").limitFirst(100)
$ Grep("...").limitLast(100)
```
### Counting hits


When working with big files, one can also decide to initially do a count of hits,
to narrow down the search terms before doing an actual search.


The following example shows a function for searching through a set of java files and
counting the hits in each, summing these up, using Grep.fileCount() function instead
of Grep.file(), which produces result lines.


Counting hits is not subject to the limits above, as those exist in order to
avoid uncontrolled memory use.

```
Input("Enter search term").get =st
Grep(st) =grep
ProjectDir.allFiles->f
assert(f.name.endsWith(".java"))
grep.fileCount(f) =count
out(count)
| _.sum
/SearchCount
```
### Limiting search to a few files


When working with huge data sets, we can count the number of hits in each file. This
could be presented as a sum, as above, or as a sorted list, displaying the number of hits
per file.

```
Input("Enter search term").get =st
Grep(st) =grep
ProjectDir.allFiles->f
assert(f.name.endsWith(".java"))
grep.fileCount(f) =count
out(Int(count,f))        # <--- see section on "Generalized sorting"
| _.sort->x
report(x.data.name, x)
/ShowHitCount
```

The next step is then to extend the regular search function, with an input for (part of)
the file name(s) to search:

```
Input("Enter search term").get =st
Grep(st) =grep
Input("(Part of) file name").get =fn
ProjectDir.allFiles->f
assert(f.name.endsWith(".java"))
assert(f.name.contains(fn))    # <--- new
grep.file(f)->line
report(line.file.name, line.lineNumber, line)
/Search
```
# Searching log files / DateSort


Lines written to log files often will start with date and time. Further, actions that we want to
trace may span several log files, for example when messages are sent between different services,
each with its own log. To clearly trace such activity, we search the log files for some text,
and then sort the lines on the date/time at the start of each line.


A custom function / object has been created for this purpose, to make it easy to use.

```
Input("Enter search term").get =st
Grep(st)=grep
ProjectDir.allFiles->f
assert(FileQualified(f))
grep.file(f)->line
out(line)
|
=lines
DateSort.asc(lines)->line
report(line.file.name, line.lineNumber, line)
/SearchLog
```
# Generalized sorting


The List object has a function sort(), which does one of two things:



- if the list contains only numbers (int or float or a mix), sorts them by number value


- ... otherwise, sort as strings



The result is a new list. To sort in reverse order, we just apply the function ".reverse()"
on the finished list.


The trick to sorting is to 
**make all types of objects to be sorted into a number or a string**.


This is done by three wrapper functions called "Int()", "Float()" and "Str()". They create objects that
are subclasses of the regular int, float and
string types, but also contain a "data" member that can be extracted after sorting, via the ".data()"
function.

## Example: file size


Let's sort the files in the current directory so that the biggest files are listed first.

```
$ Dir.files->f out(Int(f.length,f)) | _.sort.reverse->x out(x.data)
```

For each file, we output an Int wrapper object, with value set to file length, and data
pointing to the file. The resulting list is sorted, reversed, then iterated to output
the data values, which are the original File object.


Could also sort on time, as File.lastModified is another int value, with CFT int values
corresponding to long in Java.

## Example: file name


To sort on file names, we use the Str() wrapper function / object.

```
$ Dir.files->f out(Str(f.name,f)) | _.sort-> out(x.data)
```
# Dictionary objects / Dict()


When working with multiple projects, or sources of log files, we want to quickly flip
between directories, file types and perhaps other settings. This is done using dictionary
objects.


Dictionary objects are key-value stored, like Map in Java.


Maintaining these types structures is best done when editing the script file with an editor.

```
Dict
.set("dir",Dir("/home/user/project1"))
.set("types","java txt".split)
/Project1
Dict
.set("dir",Dir("/home/user/project2"))
.set("types","js css html".split)
/Project2
Project1
/CurrProject
```

Now we can build code that searches files and performs other operations on CurrProject.


Switching between projects is done by redefining the CurrProject function.
```
$ Project2
$ /CurrProject!
```

To get a named value from a Dict object, use the get(name) function.

```
$ Dict.set("a",23).get("a")
<int>
23
```
# Date and time processing

## Milliseconds


If we want to produce a list of files modified within the last 30 minutes, we can do this
easily, using the global function currentTimeMillis, and the File.lastModified function.

```
Dir.files->f assert(currentTimeMillis-f.lastModified>30*60*1000) out(f)
/FilesModifiedLast30Minutes
```
## The Date object


The Date() function can be invoked without parameters, producing a Date object that represents
current date and time, or it can be called with an int value, which is milliseconds, such as
that returned from File.lastModified. Functions exist to decide if a date is before or after
another date, as well as for accessing individual properties, such as year, day of month, and so
on.


To list all files that were changed between two date/times now becomes easy

```
P(1,Date)=fromDate
P(2,Date)=toDate
Dir.files->f
Date(f.lastModified) =fileDate
assert(fileDate.after(fromDate) && fileDate.before(toDate))
out(f)
/FilesBetweenDates
```
## The Date.Duration object


The Date object in turn contains a function Duration() which creates a Date.Duration object. This
is both output from Date.diff, which calculates the amount of time between two dates, and used
as input to the Date.add and Date.sub functions, which are used to calculate other dates.


Example: calculating the date (and time) 300 days ago

```
$ Date.Duration.days(300) =x Date.sub(x)
```
# Session persistent data / ValDef / Val


When working interactively with large sets of data, we have the option of saving those
data into a session persistent data store. Two global functions let us define
a named value, and access it.

```
$ ValDef("a",12)
$ Val("a")
<int>
12
```

Being tied to the session, these data are lost when quitting CFT.

# Synthesis

## The problem


If we use "cd" and "ls" to move
to a directory, and want to create a function that works on files or subdirectories under
that location, we have to take care.


The issue is that we can not just say

```
Dir.allFiles->f ...
```

... because the Dir() function returns the current directory, which may change.

## Creating code from values


This is where the 
**syntesis** functionality comes in. The most often used variant takes
the form of two "colon commands".



- The :syn command syntesizes code from the last result.


- The :NN  (where NN is an integer) syntesizes the indicated element of the last result list. If
last result is not a list, an error is reported.


## Example using :syn

```
$ cd ..
# /home/roar
<obj: Dir>
roar/ d:61 f:33
$ cd project1
# /home/roar/project1
<obj: Dir>
project1/ d:0 f:0
$ :syn
synthesize ok
+-----------------------------------------------------
| .  : Dir("/home/roar/project1")
+-----------------------------------------------------
Assign to name by /xxx as usual
$ /DirProject1
```

When we use "cd" to change to a directory, it returns a Dir object. The shell remembers the last
result value, and the ":syn" attempts to create code representing that value in as direct a way as
possible. If this succeeds, it inserts the generated code line tino the "code history", as the
last command, which means it can now be assigned a name, for example "DirProject"


Calling function "DirProject" will now always generate a Dir object pointing to the same
directory, and is no longer dependening on current directory.

## Example using :NN


To synthesize a single element when the last result was a list, use :NN, as follows

```
$ ls
<List>
0: runtime/              | d:2 | f:12
1: CallScriptFunc.java   | 1k  | 1398  |             | 2020-02-28 22:38:09
2: CodeHistory.java      | 10k | 10980 | 2d_22:23:24 | 2020-06-07 12:57:49
3: CommandProcessor.java | 0k  | 365   |             | 2020-02-28 22:38:09
$ :2
synthesize ok
+-----------------------------------------------------
| .  : File("/home/roar/Prosjekter/Java/ConfigTool/src/rf/configtool/main/CodeHistory.java")
+-----------------------------------------------------
Assign to name by /xxx as usual
```

If the last value was not a list, the ":NN" command will fail with an error.

# Repeat last program line


The last program line entered (not colon commands or function name assignment) can be
repeated by entering "." (dot), then possibly be followed by additional code.


As the synthesize functions create a new code line, they then insert it into the history as
the last command, which means it can be assigned a name by "/name", but also that it can
be immediately run using the dot command, and extended on the fly, for example like this.

```
$ ls
<List>
0: runtime/              | d:2 | f:12
1: CallScriptFunc.java   | 1k  | 1398  |             | 2020-02-28 22:38:09
2: CodeHistory.java      | 10k | 10980 | 2d_22:23:24 | 2020-06-07 12:57:49
3: CommandProcessor.java | 0k  | 365   |             | 2020-02-28 22:38:09
$ :2
synthesize ok
+-----------------------------------------------------
| .  : File("/home/roar/Prosjekter/Java/ConfigTool/src/rf/configtool/main/CodeHistory.java")
+-----------------------------------------------------
Assign to name by /xxx as usual
$ ..more
```

Here the first dot is the File("...") code line that was synthesized, then ".more" calls
the more() function on the File object.

# Output format / Cfg


Output to screen is regulated via a Cfg object. It is a session object, that contains default
settings for number of lines and line width of the current window / terminal. It's default
mode of operation is to disable wrapping, which means long lines are cut, ending with a simple '+'
to indicate this.


To change the current size of the terminal window, we may use global function Cfg() to obtain
the Cfg object, and methods to set or view the properties.

## The @term shortcut


After the introduction of short cuts, the easiest way to set the terminal window width and
height, is to enter

```
$ @term
```

This works on Linux (using stty command) and on Windows (powershell).

## Line wrapping


By default, ouput line wrapping is off, which means that lines longer than the Cfg.w gets truncated
with a '+' to indicate there is more. It can be switched on/off via the Cfg object, but there is also a
colon command ":wrap" which toggles wrapping on or off.

# Templating


CFT has multiple mechanisms that can be used to produce configuration files, as well
as automated generation of code, from shell scripts to any higher level language,
autogenerated email text, etc.

## Merging text with Dict


To merge values into a template, we use a dictionary object (Dict) combined with the
merge() function of strings. This replaces occurrences of names in the dictionary
with their values (as strings).

```
$ Dict.set("name","Julius")
$ /data
$ "Dear name".merge(data)
<String>
Dear Julius
```

The merge is based on a direct match. Often we like to mark our merge codes. The Dict
object has a function, ".mergeCodes()", which returns a new Dict object, where all names of fields
are rewritten into ${name}. Changing the template correspondingly, this eliminates the risk of
accidentally matching text not meant as merge codes.

```
$ Dict.set("name","Julius").mergeCodes
$ /data
$ "Dear ${name}".merge(data)
<String>
Dear Julius
```
## Using individual template files


The simplest way to create template text, is to enter it into a text file, which CFT
then reads and processes. The File.read() function returns a list of lines from the
file, which we then iterate over and apply the merge data.

```
File("myTemplate.txt")
/templateFile
P(1,Dict)=data
templateFile.read->line
out(line.merge(data))
/generate
```
## Script file "here" documents


The second easiest way of creating lines of text for processing with merge codes, apart from
editing a separate file, is to use a special feature of the script files, called
"here" documents. The syntax is picked up when reading the savefile, and the lines
of text are converted into code on the fly, becoming a List object.

```
<<< SomeMarker
This is
some text
>>> SomeMarker
/myTemplate
```

Calling the myTemplate function from the interactive shell, produces the following result

```
$ myTemplate
<List>
0: This is
1: some text
```
### A more complex example

```
P(1,"a")=a
P(2,"b")=b
Dict
.set("a",a)
.set("b",b)
.mergeCodes
=data
<<< SomeMarker
Value of a: ${a}
Value of b: ${b}
>>> SomeMarker
->line
out(line.merge(data))
/myMergedTemplate
```
## Using DataFile


Another mechanism for templating, particularly if the text blocks are big, is the
DataFile function and object, which processes
a single text file which can contain numerous individual templates.


The different templates are separated by a user defined selector string, and given names,
by which they are accessed from code.


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
$ DataFile(File("data.txt"),"###")
$ /df
$ df.get("A")
<List>
This is
template A
```
### Include blank lines


The function DataFile.get() returns only non-blank lines. To get all lines, use function getAll().

### Filter away comments


Also, DataFile has support for comments in the template text, which can be automatically
removed. They are defined by another prefix string as follows:

```
$ DataFile(someFile,"###").comment("//")
$ /df
```

Now all lines starting with "//" are automatically stripped from any output.

## PDict()


With many parameters to be merged into the template text, the special expression PDict() saves
us some writing. It takes a comma-separated list of value names, which are mapped to
parameter values by position, creating a Dict object.


For missing parameters, the value null is stored in the Dict.


The String.merge() function logic replaces value null for a merge field with empty string.

```
PDict("a","b").mergeCodes =data
<<< SomeMarker
Value of a: ${a}
Value of b: ${b}
>>> SomeMarker
->line
out(line.merge(data))
/myMergedTemplate
```
# Use as a calculator

## Expressions and "variables"


Having a running instance of CFT on the desktop means access to a capable calculator.

```
$ 24*60*60
<int>
86400
/x
x*365
<int>
31536000
```

As noted before, the symbol "x" does not refer to the value 86400, but to the code that
generates the value.

## Lib.Math


The global function Lib() creates a Lib object, which in turn contains functions that
create other objects, such as the Math object, which contains trigonometric functions.

## Lib.Convert


The Lib.Convert function returns another object, which contains code for lots of common
conversions. Use the help system to show all options.

```
$ Lib.Convert help
```
## Lib.Plot


The Lib.Plot function returns an object with functions for creating a primitive plot, for
visualizing data. Again, use the help system to examine options.


Note that Lib.Plot is a quick-and-dirty implementation. For better graphs, some external
package should be invoked.

```
File("/tmp/" + currentTimeMillis+".txt")
/tmpFile
tmpFile =f
Lib.Data.each(0,720)->i
f.append(""+Lib.Math.sin(i) + "," + Lib.Math.cos(i))
|
Lib.Plot.typeTimeline.readCSVFile(f).plot(File("out.png"))
f.delete
/DemoPlot
```

The above code generates an example plot as a png file in the current directory.

# Various advanced topics

## Command line args


If CFT is invoked with command line arguments, the first is the name of the script,
that is, a savefile minus the "savefile" prefix and ".txt" ending.


Then follows zero
or more command lines, on string format. Example:

```
./cft Code "S"
```
## Calling functions in external scripts


Sometimes we want to call a useful function in another script file. This is
implemented with the "call" expression below.

```
call "Script:Function" (....)
```

Parameters are given as a list of values inside ()'s and may be omitted if no parameters.

## Protecting directories and files


In order to avoid accidental delete or modifications, both Dir and File have
a function .protect() which takes an optional description string,  and
which attaches a protection status to that object.


Any Dir and File objects created from such an object, inherit the protection status.

```
Dir("src").protect("Source")
/dirSrc
```

Invalid operations result in an error, where the operation is described, along with the
protection code (String).


All source directories when searching should be protected, as well as log directories, if originals
matter.

### A protected directory does not allow



- create


- delete


- copy file into dir - includes blocking
File.uncompress when target dir is protected


### A protected file does not allow



- delete


- create


- append


- copyFrom (target)


- copyTo


- move (source or target)


## Working with text lines from stdin


If you've got some text in the copy-paste buffer that you want to work with, the
readLines() global functions can be used. It takes one parameter, which is an end-marker, which must
occur alone on a line, to mark the end.


The readLines() function returns a list of strings, which you can turn into code and save under
some function name, using synthesis.

```
readLines("XXX")
(paste or enter text, then enter end-marker manually)
XXX
&lgt;list>
0: ...
1: ...
:syn
/someName
...
```
## Differing between Windows and Linux


Calling function Sys.isWindows() is used to differ between the two in code. It does this
by checking if File.separator is a backslash.

```
Sys.isWindows
<boolean>
false
```
## Session state


The session state is where previous values to Input() are stored, as well as values
stored with ValDef(). When invoking some function in another script, that code runs in
a separate environment ("ObjGlobal"), so as to avoid cross-contamination.


Further, the separate environments for all external scripts invoked, are cached, so that
each call to a function in the same script, is run within the same environment.


This lets individual scripts have persistent session state, that is remembered between
calls, which means Input() remembers what you typed in last time, etc.

## Predicate calls


Example: to decide if a string is an integer, without
resorting to either creating a built-in predicate function like .isInt, or even
using regular expression matching, there is the 
**predicate call** functionality,
where one calls a function in a special way, resulting in a boolean value that tells
if the call was ok or not.


Dotted calls are made into predicate calls, by adding a '?' questionmark between the dot
and the function name.
```
"sdf".?parseInt
<boolean>
false
"123".?parseInt
<boolean>
true
```
## List.push()


The push() function of the List object pushes a number of value from the list onto the stack
to be assigned in "logical" order, and allows us to supply a default value if list too short.

```
$ List("x","y").split.push(3,"*")=a=b=c a+":"+b+":"+c
<String>
x:y:*
```
## Dict set strings


Reading name-value assignments from a property file or similar, is best done via the .setStr()
function on the Dict object. It strips whitespace and accepts both colon and '='.

```
Dict.setStr("a : b")
/d
d.get("a")
<String>
b
```

To process a property file, assuming commented lines start with '#', we can do
this:

```
P(1) =propFile
Dict =d
propFile.read->line
reject(line.trim.startsWith("#"))
assert(line.contains(":") || line.contains("="))
d.setStr(line)
|
d
/GetProps
```
## Dict fields as functions


For readability, values with names that are valid identifiers, and don't collide with regular
member functions, can be referenced via dotted notation, for increased readability.

```
Dict.set("a","b")
/d
d.a
<String>
b
```
## Dict.get with default value


The Dict.get() method takes an optional default-value which is returned if no value
associated with the name, but in that case the default value is 
**also stored** in the
dictionary.


Usually this makes sense only for session objects.

```
ValDef("data",Dict)
Val("data").get("a",3)
<int>
3
Val("data").keys
<List>
0: a
```
## List.nth() negative indexes


Using negative indexes to List.nth() counts from the end of the list. Using value -1 returns the
last element, -2 the second last, and so on.

```
List(1,2,3,4).nth(-1)
<int>
```
## Function parameters as List or Dict


In addition to grabbing one parameter at a time, using P(pos), we can also process the
parameter values as a list and as a dictionary.


The function parameter expression P() when used with no parameters, returns a list of
the parameter values as passed to the function.


The PDict() expression takes a sequence of names to be mapped to parameters by position,
resulting in a Dict object. Missing values lead to the special value null being stored
in the dictionary.

## The general loop statement


In addition to looping over lists, there is a general loop construct. It identifies no
loop variable, and loops forever, until break() is called. It also obeys assert()
and reject() as with list iteration.

```
0=a loop break(a>3) out(a) a+1 =a
<List>
0
1
2
3
```

If you forget to increment the variable a, or forget or create an invalid break(), then
the loop may never terminate, and CFT has to be killed with ^C

## Storing CFT data structures to file - syn() and eval()


Session persistent data are stored and retrieved with ValDef() and Val(), but are obviously
lost when the session ends.

A more persistent solution is to store a data structure to file. This is done using
the synthesis functionality, which is made available as a global function as well as the
"colon command" used before. This means we can write huge lists and sets of files and
directory objects to file, and restore it later, without going through possibly time
consuming computations.


To restore the structure, we use the global eval() function.
```
P(1)=file
P(2,"data") =data
file.create(syn(data))
/saveData
P(1)=file
eval(file.read.nth)
/restoreData
```

This can be used to save arbitrarily big structures, as long as they are synthesizable.

## String.esc() and .unEsc()


As was mentioned initially, CFT doesn't use backslash as an escape character.
However, we still require a way of converting "difficult" strings to code,
via synthesis. For this purpose, the two functions String.esc() and String.unEsc() was
created.


One rarely needs to call these manually, but they are worth mentioning, as sometimes synthesis
of a string may result in code such as this:

```
"^q^aa^a^q".unEsc
```
### Escape codes


For an escaped string, the escape character is the ^ symbol.



- "Double quotes" ^q


- 'Single quotes' / Apostrophe ^a


- Newline ^n


- Carriage Return ^r


- Tab ^t


- The ^ symbol ^^



To gives a way of creating strings with newlines inside.

```
"this^nis^na test".unEsc
<String>
this
is
a test
```
## Automating interactive functions / Sys.stdin()


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

## Running colon commands


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
## Simple line editing


When using CFT mainly in interactive mode, a primitive line editor can be used to modify
the code of an existing function, by optionally cutting off text following a pattern, as well
as optionally adding text.


The syntax is as follows

```
$ !x!       # inserts code from function x as text, then executes it
$ !x! txt   # inserts code from function x, followed by " txt", then executes it
$ !x:p!     # inserts code from function x up to but not including pattern 'p'
$ !x:p! txt
```
### Develop complex code in steps

```
$ Dir.files
$ /x
$ !x!->f out(f)
$ /x!
$ !x:out!assert(f.name.endsWith(".txt")) out(f)
$ /x!
```

Note: this only applies to single-line functions.

## Macros


In addition to inline block expressions that are executed immediately, there is the option of creating
"independent" block expressions, which are not executed immediately, but instead are considered
expressions that return a value of type Macro.


A macro is like a regular function, except it is also a value, which means it can be stored
in Dict objects, lists and sent as parameters, etc.


A macro is written in the same way as a block expression, just that the body starts with
a single asterisk (*), indicating
that the code can run anywhere, as it will always run in an isolated context, not
seeing any state of the caller, which block expressions naturally do.


To call a Macro, apply the .call() function on it, with parameters as needed, which are
picked up insicde the code in the same way as in functions.

### Simple example


Simple example, using a macro to save typing.

```
{* P(1)=name Dict.set("name",name)} =m
List(
m.call("x"), m.call("y")
)
```

See savefileLib.txt for examples of macros used to implement generic
menu system, as applied to select editor for Linux.

## CFT.props - mCat, mEdit and mMore macros


The configuration fields mCat, mEdit and mMore ("m" for macro) define macros
that are called for interactive commands cat/edit/more. This means it is possible
to redefine what edit means. Currently, mEdit calls either "Lib:e" if there is a
file, otherwise "Lib:e2" which presents a history of documents edited. The
mMore macro calls "Lib:m", while the mCat macro just calls .read on file parameter.

## CFT.props - shortcuts


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

# Various example code

## Windows PowerShell


The following code is an effective way of using PowerShell from CFT, saving lots of typing.

```
P(1)=host P(2)=cmd ## Run remote PowerShell script-block
List("powershell","invoke-command","-computername",host,"-scriptblock","{" + cmd + "}") =fullCmd
Dir.run(fullCmd)
/PSRun
# List services via PowerShell (interactive)
Input("Host").get =host
Input("Service name (including wildcards)").get =service
"get-service -name " + service =cmd  # no splitting
PSRun(host, cmd)
/PSGetServ
```
## Windows CMD


Running commands using CMD in windows, mostly requires the "/c" flag.

```
Dir("...")
/DirProject
# Add, commit and push with git
DirProject.run("cmd","/c","git","add",".")
Input("Commit message").get =msg
DirProject.run("cmd","/c","git","commit","-m",msg)
DirProject.run("cmd","/c","git","push","origin","master")
/GitPush
```
## Linux get user name

```
Dir.runCapture("whoami").nth
/GetUser
```
# Reference: Colon commands


Colon commands are best described by entering a single colon at the CFT prompt.

```
$ :
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
$
```
# Reference: Synthesizable types



- boolean


- int


- float


- string


- null


- List


- Dir


- File


- FileLine


- Date


- Date.Duration


- Int


- Float


- Str


- Dict


- Glob


- Regex




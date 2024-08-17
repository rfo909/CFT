
# CFT Reference

Last updated: 2024-08-16 RFO

v4.2.2


# ---- Using CFT as a shell


## Combine with CFT function results

In addition to the standard syntax, such as "cd /someDir/xyz" etc, the CFT shell commands
also support using the result from some CFT function. 

Say we have some functions:

```
Dir("/SomePath/logs")
/LogDir

File("/SomewhereElse/xyz/data.txt")
/DataFile
```

... then we can do this:

```
cd (LogDir)
edit (DataFile)
```


## The ":N"

The function Sys.lastResult returns the result from the previous interactive command. The
Sys.lastResult function takes an optional int parameter for when the result is
a list, to obtain value at a position in the list. 

```
lsd 
cd (Sys.lastResult(3))   # using ()'s to run CFT expression
```

Since this is a frequently used feature, there is a shorthand notation:

```
lsd         # lists directories
cd :3       # cd into directory 4 (prefixed by index 3 in list)

Dir.allFiles("*.java")   
cd :0.dir            # go to directory of a file in the output from previous command

# alternatively
Dir.allFiles("*.java")
:0.dir.cd            # same as above
```

### Complex example

The :N notation is also usable inside CFT expressions, so we can even 
do this:

```
ls *.txt
touch ("new_" + :0.name)
edit
```



## The "::"

Sometimes we have a function that returned perhaps a single string or file or directory, that
we in turn want to do something with, without the :N *lookup-in-list* syntax. For this
purpose, the *::* (double colon) is defined.

```
"xxx"
::+"yyy"   # gives xxxyyy

Dir.dirs.first
cd ::
```

## Colon commands and shortcuts

Shortcuts are ways of running CFT code, and are defined in CFT.props. 

Colon commands on the other hand,
are system commands that run completely outside the language interpreter (written in Java), such as
loading and saving scripts.


View all colon commands by typing a colon and press Enter

```
:
```

View all shortcuts by typing the '@' and press Enter

```
@
```

Shortcuts are defined in the CFT.props file.






## Symbols 


Symbols let us define persistent names for files and directories. These can be used
in expressions, and when executing external programs from the command line.
 
```
cd /some/dir
pwd
%%myDir    # symbol expression '%myDir' now refers to /some/dir
```

Then later, perhaps in a different session, as symbols are persisted across sessions, we can say:

```
cd %myDir

# or

%myDir.cd

# or access content inside that directory. When an expression such as this starts with
# a symbol lookup (the "%") the expression doesn't need be inside parantheses.

cat %myDir.files("*.java").first

# or even


```

To see all symbols, use shortcut

```
@%
```

This lists all defined symbols, and gives you the option of deleting symbols.


### Diffing files example

Symbols are useful when diffing two files at different locations:

```
cd /some/long/path
ls somefile.txt
:0     # somefile.txt in this directory is first row (:0) of result from "ls"
%%a    # remember this file as symbol 'a'

cd /where/the/other/file/exists
diff somefile.txt %a
```

## :N, :: and %x initiate expressions

Parameters to shell commands (and external programs) starting with ":" or "%" are considered CFT expressions even without
being embedded inside parantheses. Those expressions can not contain free spaces, outside of strings.
 


## Repeat last line

The single dot "." is used to rerun the last command given via interactive input to CFT. Note also that
it can be followed by additional text, which is literally appended to previous command. Example:

```
Dir.files    # returns a list of files
.            # rerun it
..length     # run command Dir.files.length 
```

Note that after that last line, the last command is now changed into

```
Dir.files.length
```




## External programs

To run external programs, we just type the commands and press Enter. As long as the external
program is not parseable and executable as a CFT script command or CFT shell-like command, it will
be passed to the external shell. For windows that defaults to Powershell, and on Linux to Bash.

```
git status .
Get-Service *tomcat* 
```


Default shells are configured in CFT.props.

### Force as external

For commands that are confused with CFT script or shell-like commands, we can *force* them into running
as external programs, by prefixing the line with a <TAB>, so that that what follows 
should be Linux or Powershell command. 

```
ls          # CFT shell-command "ls"
(TAB)ls         # Underlying OS "ls" command
```

### Parameters

External commands are managed by the internal CFT shell command parser, and so support the same notation
as the other shell commands, for using output from expressions, looking up symbols and
referring data from result of previous command. Here we use the external command "scp" (secure copy)
to illustrate

```
scp %x user@host:.               # symbol lookup
scp (GetThatFile) user@host:.    # CFT expression

ls
scp :4 user@host:.               # last-value indexed reference
```

### External program in current or parent dir

If a command starts with .\x or ./x depending on OS, this is taken to mean running a program ("x") found
at some path, starting in current directory, instead of interpreting the dot to mean repeat last command.

The same goes for ..\something or ../something.


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

### Blocking on input?

When listing jobs with @J, interactive jobs that block, waiting for input, will be tagged with

```
<stdin> 
```

### Bring to foreground

Jobs that require input must be brought to the foreground. We do this with the @JFG shortcut,
and can then interact with the Job, until one of three things happen:

1. the job completes
2. the job does not ask for input for 3 seconds - this enters Command mode, where
   we can type 'q' and press Enter to put the job into the background, or just Enter to 
   wait for up to 3 more seconds for more input.
3. as the process asks for input, instead of entering actual input, we can enter a control sequence (TAB + 'q' + ENTER)
   which puts the job into the background instead of sending data to it 
   
### Warning

Do not run external interactive programs as background jobs, such as

```
& Dir.run("top")
```

This results in the *top* command taking over standard I/O, regardless of it running in the background, as external commands
do not respect the virtualized (line-based) I/O that is used to control CFT background jobs.



## Command history

CFT remembers the last 40 commands. Display the command history using shortcut

```
@CH
```

This lists up to 100 commands, and lets you either rerun the command (in the same directory) or
just change to the directory of one of the commands, without running the command over.


## Recent directories history

A unique list of recent directories is available with

```
@H
```


## The "shell" command

This command starts an operating system shell, defaults to bash on Linux and PowerShell on windows.

When exiting that shell, we are returned to the CFT prompt.


## Path parent lookups

If we are working down a deep path, and want to access a directory somewhere down that path,
we can use the *parent lookup* notation, which looks for an element in the path,
by exact match or substring, starting at the bottom (current directory), moving up towards
the root.

Example:

```
cd /some/long/path/down/a/deep/tree
cd -down/different/path
pwd
/some/long/path/down/different/path
```


This works with all the shell functions, not just "cd":


```
cd /some/long/path/again
touch -pa/test.txt
```

Here 'pa' matches 'path' and we touch file /home/long/path/test.txt


## TAB "autocompletion"

CFT (Java) does not support reading single characters of interactive input, only complete
lines. This means autocomplete as we are used to in Bash, does not work. 

To remedy this somewhat, when entering commands, the TAB character gets replaced so that:

```
cd priv<TAB>

becomes

cd priv*
```

If there is a single directory matching this glob expression, this works as expected, otherwise
there will be an error, reporting incorrect match count.


## Terminal dimensions - the Term object


Output to screen is regulated via a Term object. It is a session object, remembering the
current size of the terminal window.

### The @term shortcut


After resizing the terminal, we need to update the Term object.

```
@term
```

This works on Linux (using stty command) and on Windows (powershell).

On Linux, the code that renders the prompt (Prompt script) checks terminal dimensions, so
that pressing Enter once after resizing the window, updates the Term object. This does
not work on Windows, because the command used takes up to half a second, which would slow
down the UI.

To access the terminal info in code, use the Term global function, which returns the Term
object, and has functions for accessing the width and height.

```
Term.w
Term.h
```



## Line wrapping


By default, ouput line wrapping is off, which means that lines longer than the Term.w gets truncated
with a '+' to indicate there is more. It can be switched on/off via the Term object, but there is also a
colon command ":wrap" which toggles wrapping on or off.



# ---- Script files

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

## Calling functions in other scripts


Sometimes we want to call a useful function in another script file. This is
implemented with with the following syntax:

```
Script:Function (...)

Example:
Lib:Header("This is a test")
```



## Examining non-current scripts


The '?' interactive command has an extended syntax that allows you to list functions inside
another script, as well as listing the code of particular function.

```
?Lib:                  # lists functions inside Lib
?Lib:m                 # displays code of function 'm' (to page through a text file)
```

## Show all known scripts


The function Lib:Scripts displays all available scripts, sorted by the directories given
in the CFT.props file.


The shortcut @scr calls this function.

```
@scr
```




# ---- Lists and loops

The List is the most important data structure in CFT. Lists are created with the global function List, which
creates a List object from the values given as parameters, or if given no parameters, returns an empty list.

Lists are also returned from many member functions, such as Dir.files or Std.Data.for(0,10,1).

## For each

Looping is primarily about iterating over lists, with the single arrow and loop variable. 

```
Dir.files->f ...
Std.Data.for(0,10,1)->i ...   # iterates over values 0-9 inclusive
```

Note that the loop variable is not a normal variable. It can not be modified inside the loop. 

### For each on non-lists

In CFT it is valid to loop over anything, not just lists. If a function takes a parameter
which may be a list of strings or a string, we can safely iterate over it, doing exactly one
iteration if the value is just a string.

The only two values that are not iterated over are boolean *false* and the *null* value.

```
"test"->x out(x)
```

This returns List("test").

To iterate over the characters of a string, use the .chars() function of Strings
to produce a List of the characters

```
"test".chars
```

This returns List("t","e","s","t").




## General loops

The loop statement initiates a loop that is not related to a List. This means it must be explicitly terminated
with break.

```
i=10 loop break(i<=0) println(i) i=i-1
```

## Controlling loops

Inside a loop, we use the following to control execution of code inside

```
assert(expr)   # continue with next iteration if expr is false or null
reject(expr)   # continue with next iteration if expr is not false and not null (inverse of assert) 
break(expr?)   # if expr is true or if no expr, then terminate the loop
```

Note that these apply to nested loops as well as non-nested loops, unless using Inner blocks. More about
that in a bit.


## Loop scope

Loops extend to one of three things:

- the end of the function
- a PIPE character, or
- when reaching the end of block, marked by the right curly brace

## Loop result

The result from a loop is always a List, which may be populated using out() to add single values to it,
or report(), which is used to add multiple values, usually to be presented interactively as neat columns.

If there are no calls to out() or report() inside a loop, the result is an empty List.

### Example, using PIPE 

```
Dir.files->f assert(f.name.endsWith(".java")) out(f) | _=>javaFiles 
```

Using the PIPE, we terminate the loop, and add code that picks up the result from the data stack, storing
it into a local variable, javaFiles. The "underscore" expression returns the last value, in this case the
list from the previous loop.

### Example using Inner block

Alternatively, we may use an Inner block, which is a piece of code that runs isolated from the outer
context.

```
javaFiles = Inner{ Dir.files->f assert(f.name.endsWith(".jaca") out(f) }
```

Loops are also terminated in local blocks, but using loops inside local blocks (not preceded by the Inner keyword)
behaves differently and sometimes unexpectedly. 

### Avoid loops inside local blocks

In a local block, the out() and report() statements affect the result list of the surrounding context. CFT detects
when code contains loops, and returns that list, and local blocks are not isolated from the outer context
as with Inner blocks.

This may work, and may produce unexpected results. First off, a local block does not have a return value, like
Inner blocks do. 

```
x={List(1,2,3)->x out(x+10)} x->i report("The number is",i)
```

This returns a List(11,12,13), not the report of "The number is" lines. The reason is that the variable x
is assigned the *null* value, and that is why the second iteration over x does nothing.

```
x=Inner{List(1,2,3)->x out(x+10)} x->i report("The number is",i)
```

Adding the Inner keyword to the block where we iterate over 1,2,3 means x becomes a list of (11,12,13), and in turn
that the second loop, where we call report() gets run as we expect it to.

### So what are local blocks for?

Local blocks are used for normal decision making, both inside loops and elsewhere. As they are not separate from the
context they exist in, they work as expected when we want to decide between adding this or that value with out()
or report(). 

```
Std.Data.each(0,10)->i if(i%2==0) { if (i>5) break else out(i) }
```

Returns list of (0,2,4)


## Nested loops

Loops can be nested, just remember that the PIPE symbol terminate ALL current loops. To have finer control, 
use Inner blocks.

```
List(1,2,3)->x "abc".chars->y out(""+x+y) | _=>result result->line out("prefix " + line)
```

Inner blocks gives us detailed control, like breaking off an inner loop without
affecting the outer loop, or producing a temporary list of data to be summarized and output through
the outer loop.

Below follows a somewhat complex example, where we are searching for a string and want to display only the first matching 
line from each file.

```
# Show first matching line from each file
# --
	P(1,readLine("search term")) => term
	Dir.allFiles("savefile*.txt")->f
		# using break to terminate Inner loop, while continuing the 
		# iteration over the files
		matches=Inner{
			f.read->line
				if (line.contains(term)) {
					out(line)
					break
				}
			
		}
		matches->line
			report(f.name,line)
/FirstMatch
```

At some point it will be easier to create helper functions ...




## Addition

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
## Subtraction


Using "-", we can remove one or more elements from a list.

```
List(1,2,3,2,1)-List(2,3)
<List>
1
1
```

Can also remove a single value from a list:

```
List(1,2,3,2,1)-2
<List>
1
3
1
```

## Removing duplicates from a list

```
List(1,1,2,2).unique
<List>
1
2
```


## List sorting


The List object has a single .sort() member function, which does the following:

- if all values are int, sort ascending on int value
- if all values are float, sort ascending on float value
- otherwise sort ascending on "string representation" of all values



Now, to sort other types of values, we use a "trick", which consists of wrapping each
value inside a special *wrapper object*, masking the original values as either int, float
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

### Int(), Str() and Float()


Similarly there is a global Str() function for sorting on strings, and Float() for
sorting on floats. Together with Int() function, these produce Str, Float and Int objects,
which are actually subclasses of the regular "String", "float" and "int" value types, with
the additional function .data() to retrieve the original value.


## List filtering with Lambda


Instead of using processing loops, filtering lists can also be done
using .filter() function of the List object. The lambda is called for each
item, and returns a modified value.

```
"12345".chars.filter(Lambda{P(1).parseInt}).sum
/t
```

Here, the lambda is used to convert strings to int values.

## Removing items


To remove items from the list, we just let the Lambda return null.

```
"12345".chars.filter (Lambda{ P(1).parseInt=>x if(x>=3,x,null) })
```

This returns a list of 3,4,5


## nth() negative indexes


Using negative indexes to List.nth() counts from the end of the list. Using value -1 returns the
last element, -2 the second last, and so on.

```
List(1,2,3,4).nth(-1)
<int>
```





# ---- Dictionaries

A dictionary object associates string names with values. The most basic way to set a value and retrieve it
again is

```
dict=Dict dict.set("a",5) dict.get("a")  # returns 5
```

## SymDict

This is a special built-in expression, which takes a list of names of local variables, and
returns a dictionary with those.


```
# SymDict example
# --
	P(1)=>user
	P(2)=>host
	SymDict(user,host)
/GetDict
```

This corresponds to the following:

```
# Without SymDict
# --
	P(1)=>user
	P(2)=>host
	dict=Dict
	dict.user=user 
	dict.host=host
/GetDict
```

## Dictionary name


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


## setStr()


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

## get() with default value


The Dict.get() method takes an optional default-value which is returned if no value
associated with the name, but in that case the default value is 
*also stored* in the dictionary.

```
data=Dict data.get("a",3)  # returns 3
data=Dict data.get("a",3) data.keys  # returns List("a")
```


## When value name is valid identifier

When storing a value in a dictionary, and the name that it is stored under is a valid identifier, and does not
crash with built-in member functions of the Dict object, we can use dotted notation, for improved readability.

```
dict=Dict dict.set("a",5)
dict.a   # returns 5
```

We can also use dotted notation when storing data inside a dictionary.

```
dict=Dict dict.a=5
dict.a   # returns 5
```

Note that we can even use dotted notation to store values with names that correspond to member functions,
such as "keys", but need to use .get() with the name as string, to get such values back.

```
dict=Dict dict.a=1 dict.keys=4 dict.keys # returns List("a","keys") which is return value from keys() member function
dict=Dict dict.keys=4 dict.get("keys")   # returns value 4
```

# ---- String escape char


CFT does not use backslash as an escape character, instead it used the "^" ("hat") character.

Synthesis required a way of converting "difficult" strings to code.
For this purpose, the two functions String.esc() and String.unEsc() was
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





# ---- Block expressions


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

```
# Example
# --
	List(1,2,3,4,5,6)->i
		if (i%2==0) {
			out(i*10)
		}
/ex
```

Returns List(20,40,60), because out() inside a local block are executed on the parent code space,
which is the "ex" function. For this reason it makes no sense partitioning the insides of a
local block into separate code spaces with the PIPE character, so this is forbidden.



## Inner blocks


An Inner block is a separate "code space", where we can do loops and call out() without
affecting the result of the caller.


They are like calling an inline function (no parameters though), as their inner
workings do not affect the caller, *except* that they have access to, and can
modify local variables.

This means the Inner block has a *return value* which can be picked up and stored in
a local variable, or be further processed in the outer context.

(Local blocks do not have return values). 

Below is an example, where we sum up the number of matches of a search term across
a number of files. Note that the Grep object implements this much more efficiently
with its fileCount() function.

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

This illustrates how Inner blocks are more general and powerful than using
the PIPE to split function bodies into code spaces.

Partitioning an Inner block with PIPE does not affect the code outside the Inner block. 



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


Local (plain) blocks for non-PIPE-separated blocks of code, typically used with "if". The code
inside executes in the same code space as outside the block, which means it can contain calls to
break() and out() as well as assert() and reject() and affect the outside loops. Local blocks have
no return value.


Inner blocks for isolated processing loops inside other code. This means that calling
out(), assert(), reject() and break() inside, has no effect on loops outside the block, only
for loops inside. The code inside an Inner block can be split into parts using the PIPE. Inner blocks
have a return value, just like functions.


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




# ---- Conditionals - if expression


Conditional execution of code is done in two ways in CFT, with the first being how we
control processing loops with assert, reject and break.


Then there is the if-exression. It takes two forms, but is always considered an expression, not a statement.
The difference between expressions and statements, is that expressions always return a value, which statements need not.

## Inline form

```
if (condition, expr1, expr2)
if (condition, expr1)
```

The first selects between the two expressions, based on the condition, evaluating and returning
expr1 if condition is true, otherwise expr2. The second conditionally evaluates expr1, or if
the condition is false, returns null.

## Traditional form

```
if (condition) stmt1 else stmt2
if (condition) stmt1
```
## Example 1


Inline form. Check if some value is null, and if it is, provide a default value

```
if (value != null, value, "defaultValue") =>value
```
## Example 2


Using traditional form to call statement "break".

```
i=1
loop
out(i)
if (i>=10) break else i=i+1
```

## Expressions are statements ...


Note that all expressions are also statements, which means the first example can be
written on traditional form:

```
value = if (value != null) value else "defaultValue"
```
## Blocks are expressions ...


Also note, that (local and Inner) blocks are expressions, which can contain statements, so we can do this, as
"break" is a statement:

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




# ---- Code spaces - "pipes"


The body of any loop is the rest of the code of the function, or until a "pipe" symbol
is found. The pipe symbol ("|") partitions code into a sequence of
*code spaces*. Loops are limited within single code spaces, so the "pipe"
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
*need* to contain loops. The
following is perfectly legal, although a little silly.

```
2+3 | | | | =>x x | =>y y | _ _ _ |
```

It returns 5.

## Result value from a code space


All function bodies in CFT consist of one or more 
*code spaces*. The return value
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
*null*.


## Nested loops


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


The only thing that terminates loops are end-of-scope, which includes the PIPE symbol.

## Out count

The Sys.outCount function returns the number of values added to the resulting list
from the current loop or nested loops. 

```
List(1,2,3)->x if(Sys.outCount>0) out(",") out(x) | _.concat
```


# ---- Function parameter default values


Custom functions can take parameters. This is done using the P() expression, which
identifies the parameter by position. Note that 
*parameter position is 1-based*.


```
P(1)=>a P(2)=>b a+b
```

This is a valid function, but entering it *interactively* fails, because it is immediately
parsed and executed, and there are no corresponding parameter values. To overcome this, 
the P() expressions take an optional second parameter, which is a default value (or any
expression). 


The *default value* expression inside P() is important for several reasons.


1. Allows the function code to execute while being developed interactively
2. Allows for default values when function is called without parameters, or when called with null-values
3. May act as documentation in the source
4. Provides an elegant way of making functions interactive and non-interactive at the same time,
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



# ---- User input


CFT contains the following for asking the user to enter regular input:

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

## Ask for secrets

This function asks for input that is not echoed to screen. It is usually wise
to ask twice, and verify the values matching.

```
password = Sys.readPassword("Enter password")
password2 = Sys.readPassword("Enter password (again)")
error(password != password2, "No match")
```

## Ask for missing function parameter

The optional default value part to the P() expression for grabbing parameters to
functions, can be used to produce functions that ask for missing values.

```
P(1,Input("Enter value").get) =>value ...
```


## Paste multiple lines

If you've got some text in the copy-paste buffer that you want to work with, the
readLines() global functions can be used. It takes one parameter, which is an end-marker, which must
occur alone on a line, to mark the end.


The readLines() function returns a list of strings, which you can turn into code and save under
some function name, using synthesis.

```
readLines(".")
(paste or enter text, then enter end-marker manually)
.
<List>
0: ...
1: ...
:syn
/Data
...
```

The ":syn" command synthesizes the List object, which means it generates code for it. We can then assign
a name, now  for it, which when run, reproduces the list of lines that were pasted.




# ---- Output to screen

```
println("a")
println("a",a,"b=",b)

print("a")
print("a",a,"b=",b)

```



# ---- Produce columns with report()


Using report() instead of out() lets us produce as output a list of strings,
where multiple parameters to report() is formatted into columns. Example:

```
Dir.files->f report(f, f.name, f.length)
```

NOTE: report(a,b,c) is really a convenience function, as it corresponds to
out(Sys.Row(a,b,c))

The Sys.Row object renders columns for values of the following types only: 

```
|String|FileLine|int|float|boolean|null|Date|Duration|
```


## Hidden columns

The report() statement allows us adding data objects that are not considered "printable", and
which are therefore not included when rendered to stdout. 

These values are still obtainable.

```
Sys.Row help

  <obj: Row>
  
  # asList() - return column values as List
  # asStringsRow() - returns Row with printable columns as strings
  # get(n) - return value of column n, defaults to 0
  # show() - display all columns
```

With each row generated by a report() loop being a Sys.Row object, we can use the show() function to
display the available data, and then use the get() function to get the data

However, all values of each Row is available via the .get(n) function of Row

The Row object has a function .show() to list out info about all columns, with type and value if "showable" according to type
list, corresponding to the presentation.


### Example

Both the @S shortcut, to search all files under current directory, and the search functions of the 
Projects script (started via @P shortcut), deliver a hidden column with the File object when
reporting matches. 

To show all columns, for example after running @S, we just type

```
:0.show

  <List>
   0: <obj: File> | 
   1: <String>    | ShellMv.java
   2: <int>       | 28
   3: <FileLine>  | public class ShellMv extends ShellCommand {
```

From this we see that column 0 is a File object.

To access it for a given row, we use the get() function.


```
@S
 :
 :
:0.get

  <obj: File>
  ShellMv.java 6k 6368 31d 2024-07-17 21:14:48
```

### Interactive use

When we search for something, and get a list of matches, we may wish to open the file of
a certain match in an editor, or copy it somewhere

To open in an editor:

```
@S
 :
 :
edit :0.get
```

To copy somewhere:

```
@S
 :
 :
:0.get
@c
```

The ":0.get" command identifies the file, and the @c shortcut copies it into the "clipboard". Now
we can navigate to another location and paste the file with @p (lower case!)

### Function reuse

Having written functions that are usually meant for interactive use, delivering formatted reports
to the user, being able to add real data like File objects and other, means the function can
also be used from code, just interested in the data, not the presentation.

Note that the Row.get function of course also returns values from the "printable" columns.



# ---- ANSI escape codes

The Curses script contains code for producing ANSI escape sequences to set text color,
bold and underline, as well as clearing the screen, and moving the cursor, which
enables drawing boxes, for example.


## Enable/disable

Since these escape sequences may not be supported on every device, the ANSI support can be both
disabled and enabled wholesale. 

```
Curses:Disable
Curses:Enable
```

Disabling the Curses script is stored permanently in the data store (Db2), and affects all
functions inside the Curses script, so they return empty strings instead of ANSI
escape sequences.



# ---- Sys.stdin()

Functions may query the user with Input("prompt").get and readLine("prompt"). If we want
to automate such functions, we can use function Sys.stdin() to buffer up any number of
input lines.

```
Sys.stdin("read-this") Input("Enter data").get
<String>
read-this
```

Both Input.get() and readLine() detect if there is buffered input, and
if so, do not display the prompt or other info. Particularly useful for Input.get(),
since buffering the empty string "" with Sys.stdin() means repeating the last value.


## Running colon commands from script code


Using the Sys.stdin() statement without being followed by Input.get() or readLine(), is just
another way of entering commands. This means colon commands are available from CFT code.

```
Sys.stdin("2+3")
<int>
5
```

This can be exploited to let a script modify itself, by redefining
functions, although that will be troublesome if those functions read input. A better
use is that of running colon commands, particularly loading scripts. This is used
frequently with shortcuts, like @P

```
Sys.stdin(":load Projects","curr")
```



# ---- The "protect" mechanism

Working in production environments, there will be directories and files that *must not* be changed
or deleted. 

Dir and File objects in CFT can have a "protect" mark set, which blocks destructive operations
such as appending data to files, delete, rename, move etc.

The protect mark is inherited by all Dir and File objects derived from a protected Dir or File. 

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

That's because the Dir object returned by the call to LogDir, is marked as protected, and
all File objects created by calling the files() function of that object, also get protected,
and as a consequence trying to delete these files fails.



# ---- The error() global function


The error() global function contains a conditional part, and if true, throws
a soft error with the string part, terminating current execution. Alternatively it can
be used without the condition, which means it will usually be used together with "if".

```
error(1+1 != 2,"this should not happen")

if (1+1 != 2) {
	error("oops again")
}
```





# ---- The onLoad function


In order for scripts to run code as the script is loaded, we can define a function
called onLoad, which is called every time the script file is loaded or reloaded.




# ---- The StateString function

If a script wants to display state information in the prompt, it can define a StateString
function. 

This is used in the Projects script to show the current project in the prompt.

The rendering of the prompt is handled by the Prompt script, which in turn gets
called via configuration in CFT.props.


# ---- Function parameters ...


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

# ---- Running external programs

## Summary


The functions for running external programs are part of the Dir object, which defines the
working directory for the program.

```
# Built-in functions via Dir object
Dir.run ( list|...)
Dir.runCapture ( list | ...)
Dir.runDetach ( list|...)
Dir.runProcess ( stdinFile, stdoutFile, stdErrFile, list|... )

# Lib script functions
Lib:runProcess(...)
Lib:run(...)
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
*Lib:runProcess*, which in turn is called from the simpler 
*Lib:run* function, which
also handles waiting for the external process to finish, before returning.


Both of these Lib functions take the same four parameters, but often only the first parameter is used, as the
rest have useful defaults.


*Lib:run* is the notation for calling a function in another script.

## Lib:runProcess utility function


This is a CFT function in the Lib script, which hides the complexities of
calling Dir.runProcess (above).

```
Lib:runProcess(List("ls","-l)) => result
```

The result object is a Dict with various system info, representing the running
process. It has two closures of interest.


A *closure* is a lambda, with a "self"-reference to a dictionary, and
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



# ---- CFT command line args


If CFT is invoked with command line arguments, the first is the name of the script to load and use as "current script", 
and it is named in the same way as for the ":load" command, that is, a savefile minus the "savefile" prefix and ".txt" ending.


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






# ---- Environment variables


Available via Sys.environment() function.

```
env=Sys.environment
Util:ShowDict(env)
```




# ---- Hiding helper functions  


In many cases, we need to create helper functions, which should not be visible as part
of the script interface, as seen from other scripts, or when just entering '?'.

This is done by defining the function as follows:

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




# ---- Value types

## Get type of value

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



## Type checking with "as"


The getType() has frequently been combined with error() to do type checking of parameters.

```
P(1)=>x
error(getType(x) != "String","Invalid value")
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

P(1) as String? => optionalStringValue  # The '?' means "or null"
P(1) as ("String int".split)?    # String, int or null
```
### Dict (type) names


The Dict object has an optional name property, either set at creation by Dict("something") or via Dict.setName("something"), and
is also the way *classes* are implemented in CFT, containing the class name.

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



## Note: "as XXX" is separate expression

The "as something" is an expression working with a value from the stack. This means it can be used
to type check any value, such as return value from a function. 

```
a=3 as int
```

What happens here, however, is *not* that the value (3) is type checked before being assigned, but
instead that the assignment is an expression, which returns the assigned value, which is then
type checked. The outcome is the same in this case, but this distinction may cause troubles.

### What about ... ?

```
a=(3 as int)
```

The parser will fail here, complaining it expects right parantesis after 3, since it does not know how
to process 

```
a=(expr expr)
```

To fix this, we remember that *blocks* in CFT are expressions, and they contain a sequence of statements,
and finally that expressions are also statements.

```
a={3 as int}
```

:-)






# ---- Synthesis

## Creating code from values


The *synthesis* functionality comes in the following variants. 



1. The :syn command syntesizes code from the last result
2. The :NN  (where NN is an integer) syntesizes the indicated element of the last result list. If
last result is not a list, you get an error.
3. The global function syn() 


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

## Using syn()

Synthesis is frequently employed in code, where we want to "serialize" data. To de-serialize 
those data we use the global eval() function.

Example:

```
syn(Dir.files)  # returns long string
/s

eval(s)    # returns the Dir.files list
```


## Clone any value


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




# ---- Templating


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


*Raw strings* are a special notation for strings, that is as follows:

```
a = @ this is a "raw string" ...
```

The raw string starts following the "@ " prefix, and continues to the end of the line.


*Sequence()* and 
*CondSequence()* are built-in expressions that are similar to
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



# ---- Text processing


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




# ---- The CFT datastore (Db2)


CFT implements its own primitive database, as found in Std.Db.Db2, and which is usually
interfaced via the Db2 script.

```
Db2:Set("myCollection", "field", "test")
```

The Db2 persists data to file, and handles all values that can be synthesized to code.


Also there is a Db2Obj script, which saves data objects identified by UUID's, which are
made by calling the Std.Db.UUID function.

## Collections


Apart from using static strings as collection names, another common practice is
to use the *Sys.scriptId*:

```
Db2:Set(Sys.scriptId,"name","value")
```

Sys.scriptId is better than Sys.scriptName, since there may be multiple scripts with the
same name (in different directories).

## Std.Db.Db2 vs Db2 script?


The Db2 script uses the Std.Db.Db2 object. The difference is that an object is implemented
in Java, while the script is code that runs in the interpreter. See separate section on "Objects vs Scripts".

## Synchronization


Calls to the Db2 database are thread safe, via a Db2 lock file per collection,
to prevent parallel updates, or partial reads etc.


In order to create transactions consisting of multiple Db2 calls, the Std.Db object
contains support for named locks. Example:

```
Std.Db.obtainLock("Unique Lock Name",5000) ## 5000 = wait max 5 seconds before failing
Db2:Get(Sys.scriptId,"someValue") => data
# (modify data)
Db2:Set(Sys.scriptId,"someValue",data)
Std.Db.releaseLock("Unique lock name")
```

## The Vault

The Vault is a script which handles session secrets, encrypting them with Sys.secureSessionId.

```
Vault:SessionSecretGet("That password")
```

The first time, you will be asked to enter the password twice (no echo). The next time you call this
function with the same label, the password will be returned. 

Restarting CFT invalidates the old session, and you must enter the password again once. 


# ---- Error handling


Exception handling in CFT is split into two parts, reflecting two types of
situations:



- CFT logical or data errors, called *soft errors*

- General errors, stemming from underlying Java code, network situations etc, called *hard errors*


## Soft errors


Soft errors are created by calling the error() function.


They can be specifically
caught with tryCatchSoft(), which returns a Dict containing either:

```
ok: true
result: ANY
--- or ---
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
--- or --
ok: false
msg: string
stack: List of string
```

An example of a hard error is trying to access a variable or function that doesn't
exist.

## Predicate calls


Example: to decide if a string is an integer, without
resorting to either creating a built-in predicate function like .isInt, or even
using regular expression matching, there is the 
*predicate call* functionality,
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

Beware of typing errors. Mis-spelling a function still just causes a predicate call to
return false.







# ---- Debugging


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





# ---- Lambdas and closures

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


## Calling lambda/closure

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



# ---- Classes


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






# ---- Multitasking in CFT


CFT offers the ability to run multiple processes of CFT code, via the SpawnProcess() expression.

(The shell syntax "& expr" uses the same mechanism)


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
*message passing*, in that the writer (sender) and reader (receiver)
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

## Flow control

If the potential number of processes may become very big, we may need to limit
the number of running processes at any time. 

This is done via a function in the Util script, that returns a dictionary
*object* which we use as follows. It is a dictionary, from before when CFT
had *classes*. 

The names "Lxxx" are used to help indicate that they contain lambdas (though 
strictly they are converted to closures as they are stored in the dictionary).

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




# ---- Processing JSON


The JSON script handles parsing JSON into a Dict/List structure, and
conversely, takes a Dict/List structure to JSON format again.


The JSON library is a CFT script, which means it is written in CFT, using the
Std.Text.Lexer to tokenize the input, and a plain recursive-descent parser as
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



# ---- Processing XML


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







# ---- Passwords, encryption, binary data

## Binary type

Used in connection with encryption etc. Can be created from strings, or represent
the content of a file.

```
"password".getBytes("UTF-8")
<Binary>
0x..
```

The Binary objects has member functions for converting the binary value to hex string, etc.
It can also be created from a hex string:

```
b=Binary("526F6172")
```


## Passwords


To read a password (no echo), call Sys.readPassword function.

```
password = Sys.password("Enter password")
error(Sys.password("Repeat password") != password, "no match")
```

## Encrypt / decrypt


The Std.Util object contains two functions, encrypt() and decrypt(), which both
take a password string and a salt string. These together form a complete
password, but the salt is not necessarily secret, just a way of differently initiate
the encryption engine with the same (secret) password.

```
"password".getBytes("UTF-8")  # returns Binary object
/password

Std.Util.Encrypt(password).processString("this is a message")
/x

Std.Util.Decrypt(password).processString(x)
<String>
this is a message
```

## Binary data


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

### Sys.sessionId

When starting CFT, it allocates a session UUID, which is available via

```
Sys.sessionUUID
```

This can be used if a function needs to decide if settings in Db2 were defined in
current or previous session, for example.


### Sys.secureSessionId

The secure session id is a Binary object, which is internally tagged, so that 
all CFT member functions are suppressed.

### The "Vault" script

The Vault script uses the above to persist session secrets, such as passwords, 
in a secure way. Data is stored in Db2 encrypted with the secure session id, 
which means values can only be decrypted while in current session. 

Great for caching passwords in-session in a secure way.



# ---- Lazy evaluation

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




# ---- Std.Text.Lexer


The Std.Text.Lexer objects adds
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


In the CFT functions, nodes are created via the Std.Text.Lexer.Node function.

```
Std.Text.Lexer help
# Node(firstChars?) - create empty node, possibly identifying firstChars list
# addLine(line) - processes line, adds to internal token list - returns self
# getTokenStream(rootNode) - get TokenStream object
# getTokens(rootNode) - get list of tokens
```

The nodes in turn contain the following:

```
Std.Text.Lexer.Node help
# addToken(token) - create mappings for token string, returns resulting Node
# addTokenComplex(token, charMapDict) - create mappings for complex string, returns resulting Node
# setDefault(targetNode?) - map all non-specified characters to node, returns target node
# setIsToken(tokenType?) - tokenType is an int, which defaults to 0 - returns self
# sub(chars, targetNode) or sub(chars) or sub(targetNode) - add mapping, returns target Node
```

A simple example:

```
top=Std.Text.Lexer.Node
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
# the start of some sort of data. For example for Std.Text.Lexer.Identifier, the
# "firstChars" are "a-zA-Z_". It's the letters an identifier can start
# with. Similarly we can create our own library node functions, by supplying a
# firstChars list as parameter to Std.Text.Lexer.Node
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
	Std.Text.Lexer.Node(digits) =>x
	x.sub(digits,x)
	x.setIsToken(1)
	x
/NodeInt

# Now we can for example match a IP v4 address
# --
	Std.Text.Lexer.Node =>top
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
	ident = Std.Text.Lexer.Node(firstChars)
	ident.sub(innerChars,ident)
	ident.setIsToken(1)
	ident
/Identifiers

# Whitespace
# --
	" ^t^n^r".unEsc =>chars
	Std.Text.Lexer.Node(chars) =>ws
	ws.sub(chars,ws)
	ws.setIsToken(-1)
	ws
/WhiteSpace

# Root node
# --
	Std.Text.Lexer.Node =>root
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
	Std.Text.Lexer.addLine("this is a test").getTokens(Root)->token
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
Std.Text.Node =>grade
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
Std.Text.Node =>date
Dict.set("i","0123456789") =>mappings
date.setTokenComplex("iiii-ii-ii", mappings).setIsToken
date.match("2020-09-15xxx")  # returns 10 (characters matched)
date.match("2020-009-15xxx") # returns 0 (no match)
```

Feels like Regex character classes, no?





# ---- 3D library


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
world = Std.DDD.World
ref = Std.DDD.Ref  # at 0,0,0 and scale=1, which for default camera defs means millimeters
ref=ref.scaleUp(1000)  # now working in meters
ref=ref.fwd(3).turnRight(30).down(0.5)
# new ref is placed 3 meters forward, turned right 30 degrees, and lowered 0.5 meters
```
## Brushes


The DDD library uses 3D brushes to generate content. A brush is defined as a sequence of
line segments, for example a square, which is then "dragged" through 3D-space in a
sequence of "pen down" operations.

```
boxBrush = world.Brush.box(1,1,Std.Color(255,0,0))
# creates a brush for a box centered on origo, with a certain size and color
# note the size here is "relative" and determined by the scale of the Ref's used
# when painting with the brush.
ref = Std.DDD.Ref.setScaleFactor(1000).forward(3) # forward 3 meters
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

![Wooden wheel](https://github.com/rfo909/CFT/blob/master/doc/wheel.png)


# ---- 2D library


Similar to the 3D library, the 2D library lets us draw vector graphics using a
2D Ref object that moves in the plane, using either lines or filled polygons.

## Spoked wheel (lines)


Created DDExample which draws the same wheel as the 3D version, using the LineBrush of DD.World

![Wooden wheel](https://github.com/rfo909/CFT/blob/master/doc/wheel2d.png)

## Spoked wheel (polygon fill)

Created DDExample2 which uses polygon drawing Brush of DD.World.

![Wooden wheel](https://github.com/rfo909/CFT/blob/master/doc/wheel2dpoly.png)




# ---- Internal web-server (experimental)


There is a small embedded web-server in CFT, available in the Std.Web object. It
supports GET and POST, does parameter and form input parsing.


See WebTest script. Running the Init function sets up a very simple web "site" on
port 2500 on localhost.


Remember to re-run Init after changing the code, as the
web-server runs as a Java background thread, and needs to be updated, in order to serve new
content.


A bit of a work in progress, not terribly useful, bit it sort of works.






# ---- Calling Java (experimental)


CFT lets us interface Java code via the Std.Java object. It contains functions
for identifying classes. We then look up a constructor and call it, getting a
JavaObject in return. We can also look up methods from the class object, and call
them with parameters.


Currently, for this to work, the Java code must exist in the classpath.


Example (also available in script Tests01 as function Test17):

```
Std.Java.forName("java.lang.String") => String
String.getConstructor(String).call(Std.Java.String("test")) => obj
String.getConstructor(String).call(Std.Java.String("123")) => obj2
Std.Java.Object(obj2) => paramObj
String.getMethod("concat",String).call(obj,paramObj).value
/t17
```

This function looks up the String class, then creates two instances via
the constructor that takes a String parameter. CFT strings values are converted to Java values
via the Std.Java.String() function.


Then we wrap obj2, which is a CFT value (of type JavaObject), as a Java value,
via Std.Java.Object(), and locate the concat() method of the String class.


It is invoked on
obj, with obj2 as parameter. The method call returns a JavaValue object,
which has a function value() that returns a CFT value, in this case the
concatenated string "test123".






# ---- Comments and digressions

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

# ---- Script and code size

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

### 2023-02-03 v4.0.1

Running CodeStats:main

```
Script code:      17351 lines
Java code:        35356 lines
Functions:        511
Shell commands:   22
Object types:     71
Value types:      13
```

### 2024-08-16 v4.2.2

Running CodeStats:main

```
Script code:      21491 lines
Java code:        36921 lines
Functions:        551
Shell commands:   22
Object types:     74
Value types:      13
```



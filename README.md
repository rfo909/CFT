
# CFT ("ConfigTool")

CFT is a shell-like terminal based Java app, and a functional programming language.

It was created for interactive automation of simple tasks, such as

- searching source code files
- searching multiple log files
- running external programs
- date processing
- sorting and reporting
- creating configuration files
- ...

# Download and compile

The project is currently built using Apache ANT, which results in a single JAR file.

There are no dependencies outside of standard Java libraries.

Once built, the application is started using 

```
./cft
```

On Windows, run

```
.\cft.cmd
```

To leave type ":quit" or just type CTRL-C.



# Introduction

The CFT application supports basic shell functions like "cd", "ls" and "pwd", 
but is also a programming language which lets you build functions, that
both call each other as well as a system library of 200+ functions,
belonging, both global and inside objects.

CFT is not a complete language, in the sense that one can create classes and instances
of those. Instead you work with pre-defined functions, which return objects representing
files, directories, strings, lists, dates etc, and create a hierarchy of compact functions
that automate boring things like collecting and copying log files. The number of object types
totals about 20 as of version 1.0.6.

The program prompt is a single '$'. You enter stuff, and press Enter, and it gets
executed. 

Objects all contain functions. Example:

```
$ Dir.files

# really means: 
# - call global function Dir() with no parameters
# - returns Dir object
# - call .files() function inside 
# - returns list of File objects
```

When passing no parameters to a function in CFT, there is no need to include the ()'s




# An interactive language

CFT is a functional language, consisting of functions producing objects where we call
new functions, and so on ...

It is easy to learn, and test, by entering some code, and press Enter to have it
run. CFT has full expression support with normal cardinality rules, and is great as a calculator.

```
$ 2+3
```


# Create own functions

To get full use of CFT, you will define your own functions. This can be done 
interactively at first. 

To create your first function, type the following, and press Enter.

```
$ "a b c".split
```

This produces a list of three values listed under each other. 

Then enter the following and press Enter.

```
$ /x
```

You have now created a function 'x', which you call by using its name. 

```
$ x+x
```

This produces a new list which is the sum of the previous two. 

# Create something useful

To produce a list of all Java source files found recursively under the current directory:

```
$ Dir.allFiles(Glob("*.java"))
```

That might be a candidate for a function name, to avoid having to type this more than once.
Let's call the function JavaFiles.

```
$ /JavaFiles
```

Every time we type JavaFiles and press Enter, we get a list of Java files
available from the current directory. Use "ls" and "cd" to move somewhere else,
then run JavaFiles again, observing that the list of files differ.

Typing "cd" followed by Enter returns you to the application home directory.

## Objects, not text

The list that is produced when you type JavaFiles is just a representation of the list of
files. To see the full paths of the files, type the following:


```
$ JavaFiles->f out(f.path)
```

Now you get a list of strings, each the path of a Java file. This illustrates that although
the output from JavaFiles looks incomplete, missing the paths etc, full objects are returned,
on which a number of functions can be called. To see all available functions, just create
a file object and follow it by "help". The file does not need to exist.

```
$ File("x") help
```



## Searching

Still working in the interactive interface, we can create a function to search for
a string in all Java files. First we create a helper function, which creates a Grep
object, which we then use in the main search function, which we call Search.

```
$ Grep(Input("Enter search term").get)
    :
$ /GetGrep

$ GetGrep =grep JavaFiles->f grep.file(f)->line report(line.file.name, line.lineNumber, line)
    :
$ /Search
```

After entering any code line, such as the first one above, it gets executed before you can
make the code line into a function.

The Grep() function is a global function which may take a search string as parameter. Here
we read this from the user, using the global Input() function. The Grep() function then
returns a Grep object, which becomes the return value from function GetGrep.

In the Search function we first call GetGrep then assign it to a local variable 'grep'. Then 
follows a processing loop, where we iterate over all the JavaFiles, and pass each file
as argument to the Grep object .file() function. 

This produces a list of lines, which we iterate over, using the report() statement to
generate nice output.

Note that in CFT variable assigns are "reversed", with value first (or more specifically,
found on the stack), followed by "=" and a name. Example "2 =a 3 =b a+b" returns 5.

When the code works it's time to save the script.

## Save and load scripts

```
$ :save MyScript

$ :load MyScript
```



## Display functions

To display all your defined functions, type

```
$ ?
```

To display all global functions, type

```
$ help
```

To display all functions inside an object, create an instance of that object (on the stack) 
followed by help. Examples:

```
$ Dir help
$ List help
$ File("x") help
$ Date help
$ Lib help
$ Lib.Math help
```

The global Lib function creates a Lib object, which effectively works as a name space. Inside 
the Lib object there are functions for creating still other objects, such as the Math
object, where you find math related functions for calculating sine and cosine.

If you are going to use trigonometric functions a lot in your code, and since Math is a regular
object, you can always store it in a variable.

```
$ Lib.Math =m Lib.Data.for(0,360,1)->i out(m.cos(i) + m.sin(i))
```

# Other examples

#### Counting number of lines of java code

```
$ JavaFiles->f out(f.read.length) | _.sum
```
#### Calculating date (and time) 30 days ago

```
$ Date.sub(Date.Duration.days(30))
```

#### Open remote directories (windows)

```
$ Dir("\\\\somehost\d$\someLogDir").files(Glob("*.log"))
```
#### Converting one light year to kilometres

```
$ Lib.Convert.lyToKm(1)
```
#### Doing math

```
$ 2+3*5
```
#### List all those conversions I coded an evening far far away

```
$ Lib.Convert help
```




# Edit current script file in editor

The global function savefile() returns a File object for the current script.

```
$ Dir.runDetach("notepad", savefile.path)
```

On Linux replace "notepad" with "leafpad" or "gedit" or "subl", or what have you.

If you need to run "nano", then also replace .runDetach with .run, so as not to run the
process in the background.

```
$ Dir.runDetach("leafpad", savefile.path)
$ Dir.runDetach("subl", savefile.path)
$ Dir.run("nano", savefile.path)
```

Or better yet, call the function e() inside the Lib scriptfile as follows:

```
$ call "Lib:e" (savefile)
```

This autodetects if you're on Linux or Windows, and for linux lets you select your editor (choice is remembered 
for the session).

Note that when editing a savefile, all you need do after saving changes, is run the code
directly, as CFT discoveres the file has changed, and hurries to reload the updated code as you press
Enter.


# Documentation

The above is still just a taste of how CFT works. 

Read [full documentation](doc/Doc.md) gives a detailed walktrough of most of the functionality.

Also check out the example scripts under "code.examples".


# Philosophy

- Interactive programmable shell
- Compact programming language
- Programmers automation tool

# Actual use

Since getting the interpreter up and running, in 2018, functionality has been continously added. Below
is a brief list of some of the otherwise complicated and/or boring tasks CFT has helped
me solve.

- searcing through the source code (including Java, HTML, CSS and JS) of multiple and differing projects, with quick switching between them
- some deployment, copying sets of files to multiple targets
- starting and stopping services with PowerShell on multiple remote servers
- search through multiple log files, and get single time-sorted match list
- identifying historic (zipped down) logs by date and time, copy to temp directory, unzip and search
- start and stop, as well as generate stats from sets of Node processes used for stress testing MongoDb HA setup
- initializing and cloning VM's under KVM (Linux)
- run inside new VM's to set up correct hostname and netplan (Ubuntu Server)
- generate the doc/Overview.txt file from Doc.html

# Status

Core is stable, and has been backwards compatible since October 2019, when global
function list() became current List(). Development is focused on new objects and 
new global / member functions operating orthogonally with existing functionality.



# CFT ("ConfigTool")

CFT is a shell-like terminal based Java app, and a programming language.

It was created for interactive automation of simple tasks, such as

- searching source code files
- searching multiple log files
- running external programs
- date processing
- sorting and reporting
- creating configuration files
- ...

# Download and compile

The project is written in Java and built using Apache ANT, which results in a single JAR file.

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
both call each other as well as a system library of 200+ functions, both global and inside objects.

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

CFT accepts lines of input, with code which is executed. There is full expression 
support with normal cardinality rules, and so CFT is great as a calculator.

```
$ 2+3
```


# Create own functions

To get full use of CFT, you will define your own functions. This can be done 
interactively at first. 

To create your first function, type the following, and press Enter.

```
$ Dir.allFiles(Glob("*.java"))
```

This produces a list of all java files under the directly and indirectly under current directory,

After the list of files has displayed, we may name this code line, creating a function:

```
$ /JavaFiles
```

Every time we type JavaFiles and press Enter, we call the JavaFiles function and get a list of Java files
available from the current directory. Since JavaFiles takes no parameters, the ()'s are optional.

To create functions that take parameters, read the doc.

Now use "ls" and "cd" to move somewhere else,
then run JavaFiles again, observing that the list of files differ, as the "Dir" function always returns the
current directory.

To return to the CFT home directory, type just "cd" and press Enter.

## Note: objects, not text

The list that is produced when you call JavaFiles is just a representation of the list of
files. To see the full paths of the files, type the following:


```
$ JavaFiles->f out(f.path)
```

The arrow indicates a loop, followed by a loop variable.

Now you get a list of strings, each the path of a Java file. This illustrates that although
the output from JavaFiles looks like text, it is really a list of File objects, each with
functions we can call, such as the .path function.

To see all available functions for File objects, we can create
a File object using the global File() function, and follow it by "help". The file does not need to exist.

```
$ File("x") help
```



## Searching

Still working in the interactive interface, we can create a function to search for
a string in all Java files. First we create a helper function, which creates a Grep
object, which we then use in the main search function, which we call Search.

```
$ Grep(readLine("Enter search term"))
    :
$ /GetGrep

$ GetGrep =grep JavaFiles->f grep.file(f)->line report(line.file.name, line.lineNumber, line)
    :
$ /Search
```

After entering any code line, such as the first one above, it gets executed before you can
make the code line into a function.

The Grep() is a global function which may take a search string as parameter. Here
we read this from the user, using the global readLine() function, which expects a 
prompt string as parameter. The Grep() function then
returns a Grep object, which becomes the return value from function GetGrep.

In the Search function we first call GetGrep then assign it to a local variable 'grep'. Variable
assignment is "reversed" in CFT, as it is stack based. At any time we enter "=" and an identifier,
it means grabbing the topmist value off the stack and storing it in a local variable. 

Example "2 =a 3 =b a+b" returns 5.


Then follows a processing loop, where we iterate over all the JavaFiles, with 'f' being the
loop variable, and pass each file as argument to the Grep object function .file(). 

This produces a list of lines, which we also iterate over, and for each produce output
by calling report() to generate nice formatted output.

If we wanted the output to display the path of the files, we'd just type line.file.path instead
of line.file.name inside the call to report().


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


# Goals

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
function list() was renamed to the current global function List().


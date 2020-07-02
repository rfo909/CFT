# CFT ("ConfigTool")

CFT is a shell-like terminal based Java app, and a full programming language.

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
both call each other as well as a system library of over 200 member functions,
belonging to all kinds of objects.

The most central object types are Dir, File and List. There are also strings, integers,
floats and booleans, plus still a few, totalling about 20 as of version 1.0.6.

The program prompt is a single '$'. You enter stuff, and press Enter, and it gets
executed. 

Objects all contain functions, such that

```
$ Dir.files

# really means: 
# - call global function Dir() with no parameters
# - get Dir object
# - call .files() function inside 
# - get list of File objects
```

When passing no parameters to a function in CFT, there is no need to include the ()'s


## Teaser :-)

By following the steps in this README file, you will create three single-line functions
that when run, ask you to enter a search string, and then searches through all your source
files (Java used in example), presenting a formatted result with columns for file, line number
and matching lines.

For more complex searches across multiple file types at once, excluding certain directories
and so on, read the full Doc.html under ./doc directory.




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

Typing "cd" folloed by Enter returns you to the application home directory.

Oh, and its "ls" to list files, regardless of running on Linux or Windows. 

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
$ File("x.txt") help   # file does not need to exist
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
$ Dir("\\somehost\d$\someLogDir").files(Glob("*.log"))
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

Note that when editing a savefile, all you need do after saving changes, is run the code
directly, as CFT discoveres the file has changed, and hurries to reload the updated code as you press
Enter.


# Documentation

The above is still just a taste of how CFT works. 

The file doc/Doc.html gives a detailed walktrough of most of the functionality.


# Philosophy

- Interactive programmable shell.
- Compact programming language.
- Programmers automation tool.



# CFT ("ConfigTool")

CFT is a terminal based Java application, for interactive automation of simple
tasks, such as searching, moving files, checking logs. 

## Introduction

The CFT application supports basic shell functions like "cd", "ls" and "pwd", 
but is also a programming language which lets you build functions, that
both call each other as well as a system library of over 200 member functions,
belonging to all kinds of objects.

The most central object types are Dir, File and List. There are also strings, integers,
floats and booleans, plus still a few, totalling about 20 as of version 1.0.6. 

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


## Initial requirement

CFT was developed to do small scale automation of tasks such as:

- searching source code files
- searching multiple log files
- file copy, rename, move
- running external programs such as Powershell and git
- grouping files by name
- date processing
- sorting and reporting


# Download and compile

The project is currently built using Apache ANT, which results in a single JAR file.

There are no dependencies outside of standard Java libraries.

Once built, the application is started using ./cft (Linux) or .\cft.cmd (Windows). To leave
type ":quit" or just type CTRL-C.

Please read the comprehensive document "Doc.html" stored under the ./doc directory, for
a detailed introduction. CFT also contains an interactive help-function, to list both
global functions and member functions inside various types of objects. 



# Interactive use

CFT is an interactive shell, which
produces a simple '$' prompt. Below are some examples interactive use.
```
$ ls          # list current directory
# cd someDir  # chance current directory
# cd ..       # change current directory
$ 2+3         # using CFT as a calculator
$ help        # show global functions
$ Dir help    # show Dir object functions
$ "x" help    # show string functions
$ List help   # show list functions
```

# A functional language

CFT is a functional language, consisting of functions producing objects where we call
new functions, and so on ...

# Create own functions

We also create our own functions, which can be done interactively, or by editing
the save file ("script file"). To create your first function, type the following, and press
Enter.

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
then run JavaFiles again.

Type "cd" plus Enter to return to application home directory.

Oh, and its "ls" regardless of running on Linux or Windows. 

## Searching

Still working in the interactive interface, we can create a function to search for
a string in all Java files. First we create a helper function, which creates a Grep
object, which we then use in the main search function, which we call Search.

```
$ Grep(Input("Enter search term").get)
$ /GetGrep

$ GetGrep =grep JavaFiles->f grep.file(f)->line report(line.file.name, line.lineNumber, line)
$ /Search
```

The Grep() function is a global function which takes a string, which is read from the user,
and returns a Grep object. 

In the Search function we call GetGrep then assign it to a local variable 'grep'. Then 
follows a processing loop, where we iterate over all the JavaFiles, and for each call
the .file() function inside the grep object, which produces a list of lines. We iterate
over those as well, and use report() to generate nice output.

In CFT variable assigns are "reversed". Example "2 =a 3 =b a+b" returns 5.

When the code works it's time to save the script

```
$ :save MyScript
```

To load later, naturally type

```
$ :load MyScript
```

To list all your functions, type:

```
$ ?
```


# Other examples

## Counting number of lines of java code

```
$ JavaFiles->f out(f.read.length) | _.sum
```

## Calculating date (and time) 30 days ago

```
$ Date.sub(Date.Duration.days(30))
```

## Open remote directories (windows)

```
$ Dir("\\somehost\d$\someLogDir").files(Glob("*.log")

```

## Converting one light year to kilometres

$ Lib.Convert.lyToKm(1)

## Doing math

$ 2+3

## List all those conversions I coded an evening far far away

```
$ Lib.Convert help
```

## To open an editor with the savefile

```
$ Dir.runDetach("notepad", savefile.path)
```
If on Linux, enter "leafpad" or "gedit" or "subl", or what have you. If you need to run "nano",
then replace .runDetach with .run, so as not to run the process in the background.

Note that when editing a savefile, all you need do after saving changes, is run the code
directly, as CFT discoveres the file has changed, and hurries to reload the updated code as you press
Enter.


# Documentation

The above is still just a taste of how CFT works. 

The file doc/Doc.html gives a detailed walktrough of most of the functionality.





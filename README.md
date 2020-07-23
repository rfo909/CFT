
# CFT ("ConfigTool")

CFT is a shell-like terminal based Java app, and a programming language, created to interactively
build code for all kinds of automation, including copying files, searching source code trees and
running external commands.


```
P(1) =host # Check that host responds to ping 
	true =ok 
	Dir.runCapture("ping","-c","1",host)->line 
		when(line.contains("0 received"),{false =ok}) 
	| 
	ok
/PingOk

P(1,12) =hours # Get recently modified java files
	Date.sub(Date.Duration.hours(hours)) =dateLimit
	
	Dir.allFiles(Glob("*.java"))->file 
		Date(file.lastModified) =fileDate
		assert(fileDate.after(dateLimit))
		out(file)
/JavaFilesRecentlyModified
```

# Download and compile

The project is written in Java and built using Apache ANT, which results in a single JAR file. It runs on both Linux and 
Windows, and has no dependencies outside of standard Java libraries.

Once built, the application is started 

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

CFT is not a complete language, as one can not create classes. 

Instead CFT offers a number of global functions which return relevant objects representing files,
directories, strings, dates and so on.

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

CFT reads single lines from the user, and as we press Enter, the line is interpreted. 

There is full expression support, with normal cardinality rules, so CFT can be used as
a desk calculator. Interactivity makes it easy to experiment

```
$ 2+3*5
$ "a b c d".split
$ List(1,2,3).concat("x")
$ ls
$ cd ..
```


# Create own functions

The power of CFT is defining functions. This can be done 
interactively at first, later you may select using an editor. 

Type the following, and press Enter.

```
$ Dir.allFiles(Glob("*.java"))
```

This produces a list of all java files directly and indirectly under current directory,

After the list of files has displayed, we name this code line, creating a function:

```
$ /JavaFiles
```

Every time we now type JavaFiles and press Enter, we call the JavaFiles function and get a list of Java files
available from the current directory. Since JavaFiles takes no parameters, the ()'s are optional.

To create functions that take parameters, read the doc.


## Objects - not text

The list that is produced when you call JavaFiles is just a representation of the list of
file objects. To see the full paths of the files, type the following:


```
$ JavaFiles->f out(f.path)
```

The arrow indicates a loop, followed by a loop variable.

Now you get a list of strings, each the path of a Java file. This illustrates that although
the output from JavaFiles looks like text, it is really a list of File objects, each with
functions we can call, such as the .path function.


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

The colon indicates that there is output, and in this case also input, when the code asks you
to enter search term, as the line is interpreted.

Once the line works, we name them, using the forward slash and a name, creating functions.



The Grep() is a global function which may take a search string as parameter. Here
we read this from the user, using the global readLine() function, which expects a 
prompt string as parameter. The Grep() function then
returns a Grep object, which becomes the return value from function GetGrep.

In the Search function we first call GetGrep then assign it to a local variable 'grep'. Variable
assignment is "reversed" in CFT, as it is stack based. At any time we enter "=" and an identifier,
it means grabbing the topmost value off the stack and storing it in a local variable (inside current function). 


Then follows a processing loop, where we iterate over all the JavaFiles, with 'f' being the
loop variable. We pass each file as parameter to the Grep object function .file(). 

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
$ "" help
$ 1 help
$ Dir help
$ List help
$ File("x") help
$ Date help
$ Lib help
$ Lib.Math help
$ Sys help
```

The global Lib function creates a Lib object, which effectively works as a name space. Inside 
the Lib object there are functions for creating still other objects, such as the Math
object, where you find math related functions for calculating sine and cosine.

Sys is another such namespace function / object.

# Some more examples

#### Counting number of lines of java code

```
$ JavaFiles->f out(f.read.length) | _.sum
```
#### Sum sizes of files (bytes)

```
$ JavaFiles->f out(f.length) | _.sum
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




# Edit current script file in editor

If the current script has been saved, you can always edit it by entering the following:

```
$ @e
```

This is a configurable shortcut. To see all shortcuts, just enter:

```
$ @
```


# Documentation

The above is still just a taste of how CFT works. 

Read [full documentation](doc/Doc.md) gives a detailed walktrough of most of the functionality.

Also check out the example scripts under "code.examples".


# Goals

- Interactive programmable shell
- Compact programming language
- Programmers automation tool




# CFT ("ConfigTool")


CFT is a shell-like terminal based Java application, and a programming language, created to interactively
build code for all kinds of automation.

```
Dir("/home/user/projects/whatever/src")
/SourceDir


"c h cpp".split
/FileTypes


SourceDir.allFiles->f assert(FileTypes.contains(f.name.afterLast("."))) out(f)
/SourceFiles


Grep(Input("Search term").get)
/GetGrepObj
 
 
grepObj=GetGrepObj SourceFiles->f 
	grepObj.file(f)->line 
		report(line.file.name, line.lineNumber, line)
/Search
```

# Download and compile

The project is written in Java and built using Apache ANT, which results in a single JAR file. 
It runs on both Linux and 
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

The CFT application supports basic shell functions like "cd", "ls" and "pwd", but
is primarily a Domain Specific Language (DSL) for automation.

It is object oriented, as opposed to traditional -ix and -ux shells such as bash, which process text.

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

Parantheses are optional when calling functions with no parameters, for compact and readable syntax.



# An interactive language

CFT reads single lines from the user, and as we press Enter, the line is interpreted. 

There is full expression support, with normal cardinality rules, so CFT can be used as
a desk calculator. Interactivity makes it easy to experiment.

```
$ 2+3*5
$ "a b c d".split
$ List(1,2,3).concat("x")
$ ls
$ cd ..
```


# Create own functions

The power of CFT is defining own functions. This can be done 
interactively at first, later you may select using an editor. 

Type the following, and press Enter.

```
$ Dir.allFiles(Glob("*.java"))
```

This produces a list of all java files directly and indirectly under the current directory,

After the list of files has displayed, we name this code line, creating a function:

```
$ /JavaFiles
```

Every time we now type JavaFiles and press Enter, we call the JavaFiles function and get a list of Java files
available from the current directory. Since JavaFiles takes no parameters, the ()'s are optional.

To create functions that take parameters, read the doc.



### Terminal window size

When running searches, you may find that long lines wrap and mess up the screen.

CFT needs to know the terminal windows size, to determine where to cut the lines if wrapping is off,
which it is by default. To get the terminal settings, enter

```
$ @term
```

This should work on both Linux and Windows. To turn wrapping on or off, enter

```
$ :wrap
```

#### About these commands

The first is a global shortcut, which means it runs code. List them all by entering

```
$ @
```

Shortcuts are defined in the CFT.props file. 

The second is a "colon command", which are system commands outside the programming language. To
list all, just type

```
$ :
```


## Save and load scripts

```
$ :save MyScript

$ :load MyScript
```

## Start a new script

To create a new empty script, just type

```
$ :new
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

All values in CFT are objects, including integers and strings. To display all functions inside an object, create 
an instance of that object (on the stack) followed by help. Examples:

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

The global File() function requires a string parameter. Here we just use "x", to create a valid File object.
The file does not need to exist.

The global Lib function creates a Lib object, which effectively works as a name space. Inside 
the Lib object there are functions for creating still other objects, such as the Math
object, where you find math related functions for calculating sine and cosine.

Sys is another such namespace function / object.




# Edit current script file in editor

If the current script has been saved, you can always edit it by entering the following:

```
$ @e
```

This allows you to break function code across multiple lines, and use indentation to make it more readable. 

After saving the script code in the editor, CFT automatically reloads the code when you press Enter, so there is
no need to reload.




# More documentation

[Full documentation](doc/Doc.md).

Also check out the example scripts under "code.examples".


# Goals

- Interactive programmable shell
- Compact programming language
- Automation tool



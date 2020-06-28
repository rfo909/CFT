# CFT ("ConfigTool")

CFT is a terminal based Java application.
It supports basic functions like "cd", "ls" and "pwd", but is also a programming language which lets you 
create functions that call each other, for automating repetitive tasks.

A central element is list processing, be it lists of files, list of lines from files, etc. 

- searching source code files
- searching multiple log files
- file copy, rename, move
- interfacing external programs
- grouping files by name
- date processing
- sorting and reporting
 

# Getting started

The project is currently built using Apache ANT, which results in a single JAR file.

Once built, the application us run using ./cft (Linux) or .\cft.cmd (Windows).

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

The example above calls an internal function, Dir(), which returns a Dir-objeckt. For functions
that don't take any parameters, the ()'s can be omitted.

To list all text files under the current directory:

```
$ Dir.allFiles(Glob("*.txt"))
```

This produces a list of all text files. 

Once you're happy with a line of code, and want to
store it as a named function, just enter "/name". The code can then be saved, using the 
":save" command with a save name, for example

```
$ Dir.allFiles(Glob("*.java"))
  : (lists all java files)
$ /JavaFiles
$ :save Test
```

To run this code again, just type "JavaFiles" and press Enter. To load the script file
later, just type

```
$ :load Test
```

## More examples

### Counting number of lines of java code

```
$ Dir.allFiles(Glob("*.java"))-&gt;f out(f.read.length) | _.sum
```



# Documentation

The above is just a taste of how CFT works. 

The file Doc.html under ./doc gives a detailed walktrough of most of the functionality
of CFT.





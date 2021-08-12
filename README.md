
# CFT - ConfigTool

*README last updated 2021-08-12*


```
git clone https://github.com/rfo909/CFT.git
cd CFT
mvn package
./cft
```

## Motto

*Unless you script something, it isn't real*

Manual operations are boring, introduce risk of errors, usually undocumented, can't be checked in, and should be minimized.


## What is CFT?

An interpreted and dynamically typed programming language for automation of daily tasks, 
for software developers and systems managers.

CFT runs as a terminal based REPL, with some shell-like functionality.

## Automation at all levels

Being backed by a custom functional programming language, we easily create functions, either interactively,
or more commonly, by editing script files. This is suitable for:

- copying files
- searching logs
- running programs on remote hosts
- doing stuff with PowerShell without remembering those long complex commands
- setting up software
- creating configuration files
- collecting log files
- managing services locally or remotely

Favourite functionality is stored as library scripts, for reuse later. 

CFT is written from scratch in Java, implementing a tokenizer and a custom recursive-descent parser,
that creates a tree structure, which is then executed.

Communication with remote hosts is done by running external programs: SSH and SCP on Linux, and PowerShell
in Windows. These run in daughter processes, in foreground or background.


## Why yet another script language?

The idea behind CFT is to be object oriented, like PowerShell, but with a syntax that
looks more like traditional programming languages. Also wanted to avoid the complex quote rules, and
risky variable substitutions inside strings, of traditional *ux shells, like bash. The syntax of CFT
is fairly "tiny", and easily learned. 

The language is also inspired by functional programming, in that there is no global state, which reduces
risk of errors. Tested functions tend to "just work", since they usually have no external dependencies
apart from other functions. The language supports recursion, but does not depend on it, as somehow has
become the hallmark of functional programming. 

All values in the language are objects, which contain member functions, implemented in Java. These in 
turn return other objects, such as lists of files, strings, integers. 

The user creates collections of functions in *scripts* to interact with global functions, other functions in 
same or other scripts, or inside returned object values, such as Lists, files and directories, or with the user.

Contrary to bash and PowerShell, a script file is a collection of functions, not like a "main" program on its own.
In CFT we don't call a script, we call a function inside a script.

Single functions can even be made so that they take parameters, but if (some of) those are missing, the function becomes
interactive, asking the user for missing values. 

CFT has a built-in help system that lists member functions of all objects and inside any script.

Scripts, which are saved text files with CFT functions inside, contain support for public and hidden functions, 
which regulates visibility, but not actual access. This is in order to provide a cleaner script interface when 
listed from the outside. 

The "modem noise" syntax of regular *ux shells, and the verbosity and awkward syntax of PowerShell is eliminated
in CFT. Its ease in calling external programs makes it perfect for automating PowerShell sequences, as
PowerShell is truly powerful on the windows platform.

So, CFT is another shell, but it represents a rethink, not just another almost-but-not-quite bash remake. 

# Download and compile

Written in Java and built with Maven, which results in a single JAR file. 

Tested on both Linux and Windows. 

Has no dependencies outside of the standard Java libraries.

```
git clone https://github.com/rfo909/CFT.git
cd CFT
mvn package
./cft
```


# References

[Full documentation](doc/Doc.md).

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)



# CFT - ConfigTool

*README last updated 2021-08-22*

## Motto

*Unless you script something, it isn't real*

Manual operations are boring, introduce risk of errors, usually undocumented, can't be checked in, and should be minimized.

## Terminal based

Runs in the terminal, let's us define useful functions interactively, or using editors.

## Automation at all levels

Being backed by a custom functional programming language, we easily create functions. This is suitable for:

- copying files
- collecting and searching logs
- running programs on remote hosts
- leveraging PowerShell without remembering those long complex commands
- setting up software
- creating configuration files
- managing services locally or remotely

Favourite functionality is stored as library scripts, for reuse later. 

CFT is written from scratch in Java, implementing a tokenizer and a custom recursive-descent parser,
that creates a tree structure, which is then executed.

Communication with remote hosts is done by running external programs: SSH and SCP on Linux, and PowerShell
in Windows. These run in daughter processes, in foreground or background.


## Why yet another script language?

The idea behind CFT is to be *object oriented*, like PowerShell, but with a syntax that
looks more like traditional programming languages. Also wanted to avoid the complex quote rules, and
risky variable substitutions inside strings, of traditional *ux shells, like bash. The syntax of CFT
is fairly "tiny", and easily learned. 

The language is also inspired by functional programming, in that there is no global state, which reduces
risk of errors. Tested functions tend to "just work", since they usually have no dependencies
apart from other functions. The language supports recursion, but does not depend on it, opposed to how 
this somehow has become the hallmark of "real" functional programming.

[CFT vs FP](FP.md) 

All values in the language are objects, which contain member functions, implemented in Java. These in 
turn return other objects, such as lists of files, strings, integers. 

The user creates collections of functions in *scripts* (text files) to interact with global functions, other functions in 
same or other scripts, or member functions inside returned object values, such as Lists, files and directories, 
or prompting the user for input.

Contrary to bash and PowerShell, a script file is a collection of functions, not like a "main" program on its own.
In CFT we don't call a script, we *call a function inside a script*. Scripts in CFT are really name spaces.

Single functions can of course take parameters. They can also be made so that if (some of) the parameters
are missing, the function becomes interactive, asking the user for missing values. 

CFT has a built-in help system that lists member functions of all objects and inside any script. 

The complex string handling in unix shells, with three different types of quotes, and the verbosity and 
awkward syntax of PowerShell is eliminated in CFT. 

CFT represents a rethink, not just another almost-but-not-quite bash remake. 



# Download and compile

Written in Java and built with Maven, which results in a single JAR file. 

Tested on both Linux and Windows. 


```
git clone https://github.com/rfo909/CFT.git
cd CFT
mvn package
./cft
$ 2+3
5
```


# References

[Full documentation](doc/Doc.md).

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO)


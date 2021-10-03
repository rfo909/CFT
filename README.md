
# Automation at all levels

CFT is an interpreted and interactive language for automation.

*README last updated 2021-09-09*

## Terminal based 

Acts like a shell for navigating the directory tree, etc, but is really about creating and
running functions. Small functions can be created directly from the REPL, but most functions
are created using an editor.

## Object oriented

CFT consists of a set of global functions, which return values, such as the current
directory, an empty dictionary, or a Date object for current time, and so on. These
in turn have inner functions that we can call:

```
"test".length
4
```

The key concept was to create an interactive and interpreted language inspired by
PowerShell, working with objects instead of strings, as in bash, but with a more regular syntax.

Still, CFT *does not support* user defined classes, only user defined functions.

Functions are stored in script files, which are really just name spaces. Functions call each
other, both inside a script and in other scripts. 

## No global state

CFT has no global variables, no script state. This minimizes unwanted side effects. 


# Frequent uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks

It's been in daily use since 2019 in my work as a software developer, and is very stable. 



# Example

```
# Ex: check if hosts respond to ping
#    (The arrow plus identifier is a foreach)
#    (The  /name lines define functions from the preceding code lines)

List("host1","host2","host3")
/hosts

hosts->host report(host,SSH:HostOk(host))
/checkPing 
```


# Why yet another script language?

The idea behind CFT is to be *object oriented*, like PowerShell, but with a syntax that
is more regular than both PowerShell and unix shells. 

The syntax of CFT is fairly "tiny", and easily learned. Apart from a few oddities, it is quite
regular, using dotted notation to call functions inside objects, as well as using local variables
inside functions. There are no fancy string substitution rules, which, though powerful, are also hard 
to learn and harder to read.

To keep it small and simple, there are no classes, just functions. A rich set of system objects, with functions
inside, provide rich functionality, that our functions use.

In some meanings of the term, CFT is almost functional ...

[CFT vs FP](FP.md) 

The typical use case for CFT is running it in a terminal, loading some script file, and then running
different functions from the command line. Since functions can be made to both accept normal parameters,
and ask the user for values when parameters are missing, one usually just types the name of a function, 
and presses enter. Parantheses are optional when no function parameters. 

Script files are, contrary to PowerShell and Unix script, not objects themselves. Scripts in CFT
are just namespaces for functions. We never run a script, we always run a function inside a script.

The goal of CFT is to *make scripting simpler*, by working with objects, and avoiding 
complex string substitution rules, and finally a more regular syntax than both PowerShell and traditional
Unix shells. 

CFT represents a *rethink*, not just another bash remake.


## Automation at all levels

Being backed by a powerful programming language, we easily create our own collections of functions.

- copying files
- collecting and searching logs
- running programs on remote hosts via SSH or PowerShell
- setting up software
- creating configuration files
- managing services locally or remotely

Favourite functionality is stored as library scripts, for reuse later. 

Works on both Windows and Linux.

## Proper recursive-descent parser

CFT is written from scratch in Java, implementing a fast tokenizer and a custom recursive-descent parser,
that creates a tree structure, which is then executed. 

It of course implements normal precedence rules for expressions,
so that 2+3*5 correctly becomes 17, not 25!


## Motto

*Unless you script it, (and check it in) it isn't real*

Manual operations are boring, risk errors, poorly documented, and should be avoided. 



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

[Detailed walkthrough for Windows](INSTALL_WINDOWS.md).


# References

[Full documentation](doc/Doc.md).

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO).

[Youtube HOWTO-videos](https://www.youtube.com/playlist?list=PLj58HwpT4Qy-12WjM16ALnLGEyy3kxX9r).

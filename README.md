
# CFT - ConfigTool

*README last updated 2021-08-25*

## Motto

*Unless you script it, it isn't real*

Manual operations are boring, risk errors, poorly documented, and should be avoided.

## Terminal based

CFT is an interpreted script language engine, which runs in the terminal, and lets you define 
functions interactively, or using editors. It has been in daily use since 2019, and is now considered
quite stable. 

# Why yet another script language??

The idea behind CFT is to be *object oriented*, like PowerShell, but with a syntax that
is more regular, more like traditional programming languages. 

Also wanted to avoid the complex string quote rules, 
which while compact and efficient, also is hard to read and maintain. Again, going for easier syntax. 

The syntax of CFT is fairly "tiny", and easily learned. It has a few specialties, but mostly it is 
about calling functions. 

There are no classes, only functions, which reduces complexity both for the interpreter and for the code.
Older tested functions tend to "just work", since they usually have no dependencies apart from other functions. 

[CFT vs FP](FP.md) 

Values in CFT are objects, such as files, directories, lists, dictionaries and a few others. These contain
member functions, which are written in Java. There also is a set of global functions written in Java.

The user creates collections of functions in *scripts* (text files) to interact with other 
functions in same or other scripts, member functions inside value objects, or with global
functions, for example prompting the user for input.

Contrary to bash and PowerShell, a script file is only a collection of functions, and does not act
like a "main" program on its own. In CFT we don't call a script, we *call a function inside a script*. 

Scripts in CFT are effectively just name spaces.

CFT has a built-in help system that lets you list built-in functions of all objects, as well as functions coded 
inside any script. There is also an integrated mechanism for running script functionality as parallel 
threads, for speed, for example when doing updates or checking connectivity against multiple remote hosts.

The goal is to *make scripting simpler*, by working with objects, and avoiding 
complex string substitution rules, and finally a more regular syntax than both PowerShell and traditional
Unix shells. 

CFT represents a *rethink*, not just another bash remake.


## Automation at all levels

Being backed by a powerful programming language, we easily create functions. This is suitable for:

- copying files
- collecting and searching logs
- running programs on remote hosts
- employing PowerShell without remembering those long complex commands
- setting up software
- creating configuration files
- managing services locally or remotely

Favourite functionality is stored as library scripts, for reuse later. 

CFT is written from scratch in Java, implementing a tokenizer and a custom recursive-descent parser,
that creates a tree structure, which is then executed.






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

[Full Youtube tutorial](https://www.youtube.com/playlist?list=PLj58HwpT4Qy80WhDBycFKxIhWFzv5WkwO).

[Youtube HOWTO-videos](https://www.youtube.com/playlist?list=PLj58HwpT4Qy-12WjM16ALnLGEyy3kxX9r). *NEW*

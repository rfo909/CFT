
# Automation at all levels

CFT is an interpreted and interactive language for automation.

*README last updated 2021-10-03*

## Terminal based - shell-like

The REPL makes it act like a shell, with cd, ls, pwd, cat, more and edit, for navigating the 
directory tree, and inspecting files, but it's really about 
creating and running functions. 

One-line functions can be created directly from the command line interface, but most functions
are created using an editor.

### Shortcuts

Frequently used commands or command sequences can be stored as shortcuts. These are defined in
the CFT.props file, and by default include:

```
@e       - open current script in editor
@fm      - open file manager for current dir
@home    - move to script directory
@c       - copy selection of files to clipboard
@v       - paste selection of files to current dir
```

The shortcut character '@' can be changed, also in CFT.props.


## Object oriented - functional

CFT consists of a set of global functions, which return values, such as the current
directory, an empty dictionary, or the current date, and so on. All values are objects,
and so in turn have inner functions that we can call. 

There are no primitive types, all are objects:

```
$ "test".length
  <int>
  4

$ 23.bin
  <String>
  00010111
```

The key concept was to create an *interactive and interpreted* language inspired by
PowerShell, working with objects instead of strings, as in bash, but with a more regular syntax.

To keep the language simple, CFT *does not support* user defined classes, only user defined functions.

Functions are stored in script files, which are really just name spaces. Functions can of course
call each other, both inside a script and in other scripts. 


## No global state

CFT has no global variables, no script state. This minimizes unwanted side effects. 


# Frequent uses

- check out + run build + distribute files + cleanup
- search project trees
- collect and search log files 
- various install and deployment tasks
- automate powershell command sequences

It's been in daily use since 2019 in my work as a software developer, and is stable. 



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


# Interactive help

Typing "help" lists all global functions. 

To list functions inside an object, such as string, we type:

```
$ "" help
  # after(str) - return string following given string
  # afterLast(str) - return string following last position of given string
  # before(str) - return string up to given string
  # beforeLast(str) - return string up to last position of given string
  # between(pre,post) - return string between two given strings
     :
     :
```


# Motto

*Unless you script it, (and check it in) it isn't real*

Manual operations are boring, risk errors, hard to keep documented, and should be avoided. 



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

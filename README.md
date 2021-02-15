

# CFT (ConfigTool)

Programmable shell. Terminal based. Written in Java.

Functional object oriented language. 

For all levels of automation, from searching groups of files to deploying software with dependencies.

Powerful templating functions for generating custom configuration files.


[Full Youtube tutorial](https://www.youtube.com/channel/UCT2V2_xjtUVzISdT0YjwZ_Q)


# Download and compile

Written in Java and built using Apache ANT, which results in a single JAR file. 

Tested on both Linux and Windows. Has no dependencies outside of the standard Java libraries.


Linux: 

```
./cft

$ 2+2
   4

$ "c cpp h".split
 <List>
   0: c
   1: cpp
   2: h
$ /Types

$ List(1,2,3)->x out(x+100)
 <List>
   0: 101
   1: 102
   2: 103

#
$ Dir.allFiles->f type=f.name.afterLast(".") assert(Types.contains(type)) out(f)
  (... output ...)
$ /SourceFiles



```

To leave type ":quit" or just CTRL-C.



# Goals

- Interactive programmable shell
- Compact programming language
- Automation tool
- Extensive and up to date docs
- Growing library of code


[Full documentation](doc/Doc.md).




# CFT (ConfigTool)

Programmable shell. Terminal based. Written in Java.

Functional object oriented language. 

For all levels of automation, from searching groups of files to deploying software with dependencies.

Powerful templating functions for generating custom configuration files.


[Full Youtube tutorial](https://www.youtube.com/channel/UCT2V2_xjtUVzISdT0YjwZ_Q)


# Download and compile

Written in Java and built using Apache ANT, which results in a single JAR file. 

Tested on both Linux and Windows. Has no dependencies outside of the standard Java libraries.


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

$ Types
 <List>
   0: c
   1: cpp
   2: h

$ Dir.allFiles->f type=f.name.afterLast(".") assert(Types.contains(type)) out(f)
  (... output ...)
$ /SourceFiles


$ help
$ Dir help
$ List help
$ "" help

$ :save Test
$ :quit

```

To leave type ":quit" or just CTRL-C.



# Goals

- Interactive programmable shell
- Compact programming language
- Automation tool
- Extensive and up to date docs
- Interactive help for all objects


[Full documentation](doc/Doc.md).


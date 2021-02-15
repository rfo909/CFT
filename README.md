

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
 <int>
 4


$ Dir.allFiles(Glob("*.java"))
  (...)
$ /JavaFiles


$ JavaFiles.length
 <int>
  144
$ /NumJavaFiles


$ NumJavaFiles
 <int>
  144


$ JavaFiles->f out(f.read.length) | _.sum
 <int>
  20870
$ /JavaLineCount


$ P(1,0)=>a P(2,0)=>b a*b+1
 <int>
 1
$ /calc


$ calc(3,5)
 <int>
 16


$ ?

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


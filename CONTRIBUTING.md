## Thanks for showing an interest in this software

CFT has now mostly reached the level I imagined it would when I started the project. I use it all the time myself.

Though one can always think of huge numbers of things to integrate, the key for me is to treat CFT as somewhat of
a toy language. Creating script files with much more than 10-20 functions just gets messy, as there is no
type system, nor should there be one. Also there is no way of creating classes, which is also by design, in order
to keep code and interpreter simple.

I'd like a discussion on improvements that give powerful features managed with small pieces of code.

## Ideas under consideration

Below are some ideas I have been considering for a while, without having concluded, and without having found an elegant
way of implementation. Many also "suffer" under the "programmers disease" of potentially being clever and smart, but without 
fulfilling some actual need .... :-)


#### - Parse tools

To be able to parse tokens from strings, in a way that is easily expressable in code. Creating grammars?

Parsing JSON and HTML.

#### - Detached front-end (UI)

The idea is to let the UI be a thread separate from backend processes, each running a command. The idea is to let the user 
stop million-line listings, by pressing Enter or something, which would immediately detach the Stdio of the
running process.

It turned out that adding simple limits on both Grep and the ability to let a processing loop find out how many items
have been output, is sufficient for terminating otherwise runaway searches.

#### - Capturing ^C

Would be nice when having unsaved changes. Have not found a way of doing that.

#### - Reading key presses / characters

Various curses libs offer this, but are they compatible across environments? CFT must be able to run in CMD on windows, as
well as console and terminal windows on Linux. Should also hopefully work on Mac.

#### - Testing on mac

I have almost no experience with Macs.

#### - Monitoring files

Having some special type of object, some FileMonitor, watch over log files and catch changes, possibly putting them into 
RAM, or searching for special things, as well as notifying the user that something has happened.

Again a fine idea, except the actual need has never emerged. Being able to filter files on dates and names, has been 
good enough so far.

#### - Creating investigation reports

Searching through logs, following tangents, making notes of both the data found and what we believe they mean, 
and eventually producing a report (HTML or PDF even), has along with monitoring files been an idea I had
from the very start. 

As software developers, searching logs and documenting our findings, perhaps even the paths that did not mean
what we thought, can be useful.

A simple function Sys.lastValue() which points at the output from the last command, could be a start.

Allowing free syntax commands would be even nicer:
```
  $ @ : blah blah  # add last value and comment blah blah
  $ @33 : blah blah  # add lastValue.nth(33) plus comment to investigation report
  $ @show      # show investigation tree
  $ @go XXX    # switch to different node / untested theory
```


#### - Menu system / GUI

Some shared code in the Lib script enables text menus, and can be extended. Operating a GUI, using Swing ... that's not
very tempting. Letting the CFT process be a web server, presenting a GUI via HTML and JS is preferrable,
but ... will it require lots of CFT code, huge templates and overall complexity? And what is the need?

#### - Improved graph

The graph tool (Lib.Plot) is extremely primitive. Being able to create somewhat more advanced graphs would be nice, but
the current implementation sufficed for what I was doing at the time. 

Adding support for some free external program, communicating via text files or command lines? Could work. Again, 
would depend on the need.

#### - Free text search engine

Rather than grepping through files, and somewhat in line with the point about monitoring files, is the idea of
creating a RAM database for more advanced queries, spesifically what I call X-Y-Z query, which goes like this:

- search for lines that contain X
- out of these, identify some pattern Y
- locate lines that contain Y and additional pattern Z

This search would be extremely hard to solve with just Grep, due to high number of traversals.

#### - Join out() and report()

Today, creating a function that calls report() to present formatted data excludes using the same function to
provide data (other than strings) for further processing. Some way of doing both in the same processing 
loop, would be nice.

#### - onload functions?

Some scripts require the user to be logged in as root. Checking this is easy enough, and an onload
mechanism could warn the user immediately.

#### - Environment variables

Reading enviroment variables would be nice.

Starting a command with Dir.run and with custom environment variable values would be even nicer.

I have not had the need, but this is a piece of functionality that a half descent shell "must" have, right?


#### - Imports?

Importing code from other scripts, making functions there a part of the current script? 

#### - Network support

A big topic, which may fall into different categories:

- exchange data and files between multiple CFT instances on different hosts
- establishing a network A-B-C where A sees B and B sees C, etc, avoiding network segmentation limits
- remote control
- uploading and running scripts remotely
- signing scripts?
- creating "remote" functionality at least for Dir and File and exchanging all synthesizable objects
- running CFT as daemon? 
- network security, shared secrets, encryption, certificates, authorization, ...
- CFT user privileges needed to be able to start/stop services etc?
- Creating a dangerous vector of attack?
- REST client for testing / management / monitoring - at least will not by itself require elevated user
- REST server, for JavaScript interactive client / stats / ???
- FTP in its various versions?
- SSH?

#### - Script repository

For users running CFT on multiple locations, getting a way to access scripts off some server would be nice. 

Perhaps using that primitive, no-username-or-password version of FTP, was that called "simple FTP"? 

Or just HTTP GET?

## Remember ...

The philosophy of CFT is that of providing advanced functionality in a simple wrapping, with an emphasis
on simple. Simple not only as in using a "command interface" and even (gasp!) text, but also of how
much code is required. 

As an example, creating an interface to a graphing package, it would not do having to perform 20+ calls
to an API for setting basic properties like fonts, colors and all such. 

Providing instead a (well documented) text file format, possibly with an embedded parser, for 99% of those
options, with merge codes for things like headers, is a possible way of maintaining control over
every aspect, while supporting an easy to use interface for casual needs.

 


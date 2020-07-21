## Thanks for showing an interest in this software


As of mid July 2020, CFT has now mostly reached the level I imagined it would when I started the project. I use it all the time myself.

Although one can always add stuff, the key for me is to add only things that truly expand the functionality, in the
broadest possible sense.  

I'd like a discussion on such additions.


## Ideas under consideration

Below are some ideas I have been considering for a while, without having concluded, or without having found an elegant
way of implementation. 

Many suffer under the "programmers disease" of potentially being clever and smart, but without 
fulfilling some actual need, but they may inspire useful ideas.

#### - Parse tools

To be able to parse tokens from strings, in a way that is easily expressable in code. Creating grammars?

Parsing JSON and HTML.

Let code operate more interactively, by parsing live output from ssh session with remote server.

#### - Capturing ^C

Would be nice when having unsaved changes, as working with windows ^C means "copy". Have not found a way of doing that.


#### - Testing on mac

CFT should be tested on Mac.


#### - Improved graph

The graph tool (Lib.Plot) is extremely primitive. Being able to create somewhat more advanced graphs would be nice, but
the current implementation sufficed for what I was doing at the time. 


#### - Free text search engine

Rather than grepping through files, and somewhat in line with the point about monitoring files, is the idea of
creating a RAM database for more advanced queries, spesifically what I call X-Y-Z query, which goes like this:

- search for lines that contain X
- out of each of those, identify some pattern Y
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

For users running CFT on multiple locations, getting a way to access updated scripts off some server would be nice. 

Perhaps using TFTP (Trivial FTP). 

Or just HTTP GET?


## Remember ...

The philosophy of CFT is that of providing advanced functionality in a simple wrapping, with an emphasis
on simple. Simple not only as in using a "command interface", but also of how
much CFT script code is required. 

Complex API's are okay as long as the defaults allow easy use as well. Preferrably complex API's should
be replaced by config-files, which can be adapted easily through merging in data.


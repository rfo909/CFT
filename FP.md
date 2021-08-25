
# Quasi FP

*2021-08-25 RFO*

CFT is not a functional programming language, but it is inspired 
by such.

## No classes

Most CFT user functions depend solely on other functions, either defined
in a script file, or existing inside a system object. This make them
more predictable than if they in addition were to depend on a 
hidden object state, as in OO languages, which "enable" complicated
bugs stemming from ordering of calls.  

## Externals

Some functions will modify external data in files or by manipulating
stateful external services. Then there is the Db2 data store, 
which also writes data to files.  

## Local variables only

Program code in CFT uses the imperative style, which allows variables
and loops, without needing to do recursion. But variables exist only
inside functions.

## Mutable parameters

All values are objects, including parameters to functions, which means
they may be modified inside the function.

This is a clear break with pure FP.

# Conclusion

CFT is created for powerful scripting, which means we can not 
afford neither complex abstractions, nor costly memory management
schemes.  

CFT, as with all non-FP programming languages, to some degree depends on what pure 
FP calls "side effects", to produce results. 

The design of CFT still tries to minimize the *undesired* side effects, by
letting users create functions only, banning user space hidden-state
objects. This also means prohibiting script files from acting
as objects, as is done with PowerShell and unix shells.

Making mutations to parameters depend mostly on their values, as long 
functions depend mostly on other functions. This gives a degree of
predictability.

Still, there will be complicated bugs, relating to Db2 saved
data, as well state of services, content in files, files in directories,
and so on. But the aim is to reduce the risk.


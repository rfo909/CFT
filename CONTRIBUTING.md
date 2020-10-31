## Thanks for showing an interest

There really isn't much of a plan for what CFT is going to be. I use it daily both at home
and work, and lately (Oct'20) it has been much about automation to install and
configure MongoDB and ElasticSearch clusters.

The PowerShell integration script (PS) has come a long way, and will continue to grow
to automate the (bleep) PowerShell syntax.

Also the parse tools in Lib.Text.Lexer have not used to their fullest, although there is the
initial JSON parser written in CFT.

## Possible topics

Database integration, with a proper database, not my Lib.Db hack, for stateful representation of
jobs to run, outcomes, etc. I've been considering MongoDB, but experience from work doing MongoDB
integration from Java ... not fun. 

Presentation of results is another big topic. With a proper database that might sort itself
out external to CFT.

Parallellizing is interesting. Running branches of a CFT program in separate threads,
would be helpful for throughput, when upgrading or installing software on N remote hosts. The
report model for such tasks needs to evolve into being much more database-centric.


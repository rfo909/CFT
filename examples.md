```
# Select regular file from directory
# (Helper function)
# --
	P(1,Dir) => dir
	Lib:MenuSelect(dir.files, Lambda{P(1).name}, "Select file")
//SelectFile


# Get age of file in days
# --
	P(1,SelectFile(Dir)) as File => file
	Date => now
	Date(file.lastModified) => fileDate
	Date.diff(fileDate).asDays => days
	if (fileDate.after(now),"in the future","ago") => desc
	"File change date is " + days + " days " + desc
/FileAgeDays


# Convert text file to use LF at end of lines
# --
	P(1,SelectFile(Dir)) as File => file
	file.setWriteLF.create(file.read)
/FileLF


# Convert text file to use CRLF at end of lines
# --
	P(1,SelectFile(Dir)) as File => file
	file.setWriteCRLF.create(file.read)
/FileCRLF


# Your age in days
# --
	P(1,Input("Enter birth date on format YYYY-MM-DD").get) => date
	Date.setFormat("yyyy-MM-dd").parse(date) => d
	
	List("N/A","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday") => days
	day=days.nth(d.dayOfWeek) # dayOfWeek is 1-based, and starts on Sunday
	
	"Born on a " + day + " " + Date.diff(d).asDays + " days ago"
/AgeInDays


# Check if program exists (linux)
# --
	P(1,readLine("program name")) => cmd
	Lib:run(List("which",cmd),null,true).exitCode==0
/HasProgram


# Show Dictionary content
# --
	dict=tryCatch(invalid-code)
	Util:ShowDict(dict)
/ShowDict


# Show type of object
# --
	Sys.getType(2.3)
/ShowType

```

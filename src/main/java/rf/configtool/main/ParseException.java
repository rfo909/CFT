/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main;

import rf.configtool.lexer.SourceLocation;

/**
 * Exceptions arising while parsing own code, which always relates to a source location
 */
public class ParseException extends CodeException {
    
    public ParseException (SourceLocation loc, String msg, Exception originalException ) {
        super(loc, msg, originalException);
    }
    
    public ParseException (SourceLocation loc, String msg) {
        this(loc,msg,null);
    }

    public ParseException (SourceLocation loc, Exception originalException) {
        this(loc,originalException.getMessage(),originalException);
    } 
    
    public ParseException (String msg) {
        this(null, msg);
    }
    
    @Override
    public String getMessage() {
        return "ParseException: " + super.getMessage();
    }

}

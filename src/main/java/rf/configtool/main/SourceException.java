/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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
 * Exceptions arising in own code, which can relate to a source location, should throw this. In addition, 
 * operations that may fail inside Java runtime / libs, should capture these and repackage them into
 * SourceExceptions at the earliest convenient time, ensuring proper reporting to user.
 */
public class SourceException extends CodeException {
    
    public SourceException (SourceLocation loc, String msg, Exception originalException ) {
        super(loc, msg, originalException);
    }
    
    public SourceException (SourceLocation loc, String msg) {
        this(loc,msg,null);
    }

    public SourceException (SourceLocation loc, Exception originalException) {
        this(loc,originalException.getMessage(),originalException);
    }
    

}

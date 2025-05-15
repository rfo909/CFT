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

package rf.xlang.main;

import rf.xlang.lexer.SourceLocation;

/**
 * Exception related to CFT code
 *
 */
public abstract class CodeException extends Exception {
    
    private SourceLocation loc;
    private String msg;
    private Exception originalException;
    
    public CodeException (SourceLocation loc, String msg, Exception originalException ) {
        super(msg);
        this.loc=loc;
        this.msg=msg;
        this.originalException=originalException;
    }
    
    public CodeException (SourceLocation loc, String msg) {
        this(loc,msg,null);
    }

    public CodeException (SourceLocation loc, Exception originalException) {
        this(loc,originalException.getMessage(),originalException);
    }

    public SourceLocation getLoc() {
        return loc;
    }

    @Override
    public String getMessage() {
        String s=(loc != null ? loc.toString() + " " : "") + msg;
        if (originalException != null) s+=" (" + originalException.getClass().getName() + ")";
        return s;
    }

    public Exception getOriginalException() {
        return originalException;
    }
    
    
    public String toString() {
        return getMessage();
    }
}

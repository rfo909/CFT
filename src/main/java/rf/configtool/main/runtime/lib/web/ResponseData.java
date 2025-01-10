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

package rf.configtool.main.runtime.lib.web;

public class ResponseData {

    private String contentType;
    private byte[] data;
    
    public ResponseData (byte[] data) {
        this("text/html", data);
    }
    
    public ResponseData(String contentType, byte[] data) {
        super();
        this.contentType = contentType;
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }


}


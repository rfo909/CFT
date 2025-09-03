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

import java.util.List;

public class ResponseData {

    private String contentType;
    private List<String> httpHeaders;
    private byte[] data;
    
    public ResponseData (byte[] data) {
        this("text/html", null, data);
    }
    
    public ResponseData(String contentType, List<String> httpHeaders, byte[] data) {
        this.contentType = contentType;
        this.httpHeaders = httpHeaders;
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public List<String> getHttpHeaders() { return httpHeaders; }
    public byte[] getData() {
        return data;
    }


}



package rf.configtool.main.runtime.lib.webclient; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;


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

public class ObjWebClient extends Obj {

    public ObjWebClient() {
        this.add(new FunctionGet());
    }

    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "WebClient";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "WebClient";
    }
       
    
    class FunctionGet extends Function {
        public String getName() {
            return "Get";
        }
        public String getShortDesc() {
            return "Get(host,port,path) - returns Dict";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String host=getString("host", params, 0);
            int port=(int) getInt("port", params, 1);
            String path=getString("path", params, 2);
            path=path.replace(" ","+");
            return doCall("GET", host, port, path);
        }
    }

    private Value doCall(String method, String host, int port, String path) throws Exception {

        try (Socket socket = new Socket(host, port)) {

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // HTTP GET
            String request =
                    "GET " + path + " HTTP/1.1\r\n" +
                    "Host: " + host + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String status = reader.readLine();
            Map<String,String> headers = new HashMap<>();

            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                int p = line.indexOf(':');
                if (p > 0) {
                    headers.put(
                            line.substring(0,p).trim().toLowerCase(),
                            line.substring(p+1).trim());
                }
            }

            String contentType = headers.getOrDefault("content-type", "");
            int contentLength =
                    Integer.parseInt(headers.getOrDefault("content-length", "0"));

            byte[] body = new byte[contentLength];

            int total = 0;
            while (total < contentLength) {
                int n = in.read(body, total, contentLength - total);
                if (n < 0)
                    break;
                total += n;
            }

            ObjDict dict=new ObjDict();
            dict.set("Content-Type",new ValueString(contentType));

            if (contentType.startsWith("text/html")) {
                String html = new String(body, StandardCharsets.UTF_8);
                dict.set("data", new ValueString(html));

            } else if (contentType.startsWith("application/json")) {

                String json = new String(body, StandardCharsets.UTF_8);
                dict.set("data", new ValueString(json));

            } else if (contentType.startsWith("application/octet-stream")) {

                dict.set("data", new ValueBinary(body));

            } else {

                throw new Exception("Unknown Content-Type: " + contentType);
            }

            return new ValueObj(dict);
        }
    }
}
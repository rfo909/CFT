package rf.configtool.main.runtime.lib;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.net.URL;

public class ObjRest extends Obj {

    private URL url;
    private String method="POST";
    private String basicAuthString;
    private String jsonData;
    private boolean verbose = false;



    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    public String getTypeName() {
        return "Rest";
    }

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "Rest";
    }

    public ObjRest()  {
        this.add(new FunctionUrl());
        this.add(new FunctionMethod());
        this.add(new FunctionBasicAuth());
        this.add(new FunctionJsonData());
        this.add(new FunctionVerbose());
        this.add(new FunctionExecute());

    }

    private ObjRest self() {
        return this;
    }

    class FunctionUrl extends Function {
        public String getName() {
            return "url";
        }

        public String getShortDesc() {
            return "url(string) - set url, default is POST, returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            url=new URL(getString("string", params, 0));
            return new ValueObj(self());
        }
    }


    class FunctionMethod extends Function {
        public String getName() {
            return "method";
        }

        public String getShortDesc() {
            return "method(string) - set method, default is POST, returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            method=getString("string", params, 0);
            return new ValueObj(self());
        }
    }

    class FunctionBasicAuth extends Function {
        public String getName() {
            return "basicAuth";
        }

        public String getShortDesc() {
            return "basicAuth(string) - set basic auth string, returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            basicAuthString=getString("string", params, 0);
            return new ValueObj(self());
        }
    }
    class FunctionJsonData extends Function {
        public String getName() {
            return "jsonData";
        }

        public String getShortDesc() {
            return "jsonData(string) - set input json data, returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            jsonData=getString("string", params, 0);
            return new ValueObj(self());
        }
    }

    class FunctionVerbose extends Function {
        public String getName() {
            return "verbose";
        }

        public String getShortDesc() {
            return "verbose() - enable verbose mode, returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            verbose=true;
            log(ctx,"Rest: Enabling verbose mode");
            return new ValueObj(self());
        }
    }




    class FunctionExecute extends Function {
        public String getName() {
            return "execute";
        }

        public String getShortDesc() {
            return "execute() - perform rest call, returns Dict object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueObj(executeCall(ctx));
        }
    }

    private void log (Ctx ctx, String msg) {
        if (verbose) ctx.getStdio().println(msg);
    }


    public ObjDict executeCall (Ctx ctx) throws Exception {
        ObjDict dict=new ObjDict();

        HttpURLConnection connection=null;
        if (url==null) throw new Exception("No url");
        if (method==null) throw new Exception("No method");

        log(ctx,"---- executeCall() ----");

        final String JSON = "application/json";

        try {
            log(ctx,"Creating connection to " + url.toString());
            connection = (HttpURLConnection) url.openConnection();

            log(ctx,"Setting method=" + method);
            connection.setRequestMethod(method);

            if (basicAuthString != null) {
                log(ctx, "Setting authorization to basic: " + basicAuthString);
                connection.setRequestProperty("Authorization", "Basic " + basicAuthString);
            }
            if (jsonData != null) {
                log(ctx,"Setting Content-Type: " + JSON);
                connection.setRequestProperty("Content-Type", JSON);
            }
            log(ctx,"Setting Accepts: " + JSON);
            connection.setRequestProperty("Accepts", JSON);

            connection.setDoOutput(true);
            connection.setDoInput(true);

            if (jsonData != null) {
                log(ctx,"Writing jsonData: " + jsonData);
                // Write the JSON data to the request body
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonData.getBytes("UTF-8"));
                //outputStream.close();
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            dict.set("responseCode", new ValueInt(responseCode));
            log(ctx,"Got responseCode=" + responseCode);

            //if (responseCode.toString().startsWith("2")) {
                // Read the response body
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    log(ctx,line);
                    response.append(line);
                }

                // reader.close();

                dict.set("result", new ValueString(response.toString()));
                log(ctx,"Got result: " + response.toString());
            //}
            return dict;
        } finally {
            if (connection != null) try {connection.disconnect();} catch (Exception ex)  {};
        }

    }
}

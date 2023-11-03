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



    public ObjDict executeCall (Ctx ctx) throws Exception {
        ObjDict dict=new ObjDict();

        HttpURLConnection connection=null;

        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);

            if (basicAuthString != null) {
                connection.setRequestProperty("Authorization", "Basic " + basicAuthString);
            }
            if (jsonData != null) {
                connection.setRequestProperty("Content-Type", "application/json");
            }
            connection.setRequestProperty("Accepts", "application/json");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            // Write the JSON data to the request body
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonData.getBytes("UTF-8"));
            //outputStream.close();

            // Get the response code
            int responseCode = connection.getResponseCode();
            dict.set("responseCode", new ValueInt(responseCode));

            if (responseCode % 100 == 2) {
                // Read the response body
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // reader.close();

                dict.set("result", new ValueString(response.toString()));
            } else {
                throw new Exception("Request failed. Response Code: " + responseCode);
            }
            return dict;
        } finally {
            if (connection != null) try {connection.disconnect();} catch (Exception ex)  {};
        }

    }
}

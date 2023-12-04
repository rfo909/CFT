package rf.configtool.main.runtime.lib;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ObjRestApache extends Obj {

    private String url;
    private String method = "POST";
    private String authString;
    private String jsonData;



    @Override
    public boolean eq(Obj x) {
        return x == this;
    }

    @Override
    public String getTypeName() {
        return "Rest.Apache";
    }

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    private String getDesc() {
        return "Rest.Apache";
    }

    public ObjRestApache()  {
        this.add(new FunctionReadme());
        this.add(new FunctionUrl());
        this.add(new FunctionMethod());
        this.add(new FunctionAuth());
        this.add(new FunctionJsonData());
        this.add(new FunctionExecute());

    }

    private ObjRestApache self() {
        return this;
    }

    class FunctionReadme extends Function {
        public String getName() {
            return "_Readme";
        }

        public String getShortDesc() {
            return "_Readme() - display info";
        }

        private String[] data= {
                "Apache REST client",
                "------------------",
                "",
                "The Apache Rest code provides better details when a REST call fails,",
                "but does currently not implement file upload, nor other methods apart",
                "from GET and POST.",
        };

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            for (String line:data) {
                ctx.getObjGlobal().addSystemMessage(line);
            }
            return new ValueObj(self());
        }
    }

    class FunctionUrl extends Function {
        public String getName() {
            return "url";
        }

        public String getShortDesc() {
            return "url(string) - set url, returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            url=getString("string", params, 0);
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

    class FunctionAuth extends Function {
        public String getName() {
            return "auth";
        }

        public String getShortDesc() {
            return "auth(string) - set Authorization string, returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            authString=getString("string", params, 0);
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
            if (method.equalsIgnoreCase("post")) {
                return new ValueObj(doPostApache(ctx));
            } else if (method.equalsIgnoreCase("get")) {
                return new ValueObj(doGetApache(ctx));
            } else {
                throw new Exception("Invalid method, must be POST or GET: " + method);
            }
        }
    }


    public ObjDict doPostApache (Ctx ctx) throws Exception {
        ObjDict dict=new ObjDict();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            if (authString != null) {
                httpPost.addHeader("Authorization", authString);
            }
            if (jsonData != null) {
                httpPost.addHeader("Content-Type", "application/json");
                StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
            }

            HttpResponse response = httpClient.execute(httpPost);

            int responseCode = response.getStatusLine().getStatusCode();
            String responseReason = response.getStatusLine().getReasonPhrase();

            dict.set("responseCode", new ValueInt(responseCode));
            if (responseReason != null) {
                dict.set("responseReason", new ValueString(responseReason));
            }
            String result = EntityUtils.toString(response.getEntity());
            if (result != null) {
                dict.set("result", new ValueString(result));
            }
        }
        return dict;

    }



    public ObjDict doGetApache (Ctx ctx) throws Exception {
        ObjDict dict=new ObjDict();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            if (authString != null) {
                httpGet.addHeader("Authorization", authString);
            }

            HttpResponse response = httpClient.execute(httpGet);

            int responseCode = response.getStatusLine().getStatusCode();
            String responseReason = response.getStatusLine().getReasonPhrase();

            dict.set("responseCode", new ValueInt(responseCode));
            if (responseReason != null) {
                dict.set("responseReason", new ValueString(responseReason));
            }
            String result = EntityUtils.toString(response.getEntity());
            if (result != null) {
                dict.set("result", new ValueString(result));
            }
        }
        return dict;

    }


}


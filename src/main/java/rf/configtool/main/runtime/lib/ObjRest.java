package rf.configtool.main.runtime.lib;

import rf.configtool.main.Ctx;
import rf.configtool.main.SourceException;
import rf.configtool.main.runtime.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.UUID;

public class ObjRest extends Obj {

    private String url;
    private String method="POST";
    private String authString;
    private String jsonData;
    private boolean verbose = false;

    private List<FileUpload> fileUploads = new ArrayList<FileUpload>();

    class FileUpload {
        public File file;
        public String contentType;
        public FileUpload (File file, String contentType) {
            this.file=file;
            this.contentType=contentType;
        }
    }
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
        this.add(new FunctionAuth());
        this.add(new FunctionJsonData());
        this.add(new FunctionVerbose());
        this.add(new FunctionExecute());
        this.add(new FunctionMultipartFile());

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

    class FunctionBasicAuth extends Function {
        public String getName() {
            return "basicAuth";
        }

        public String getShortDesc() {
            return "basicAuth(string) - set basic auth string (user:pass) as base64, returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected string parameter");
            authString="Basic " + getString("string", params, 0);
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

    class FunctionMultipartFile extends Function {
        public String getName() {
            return "multipartFile";
        }

        public String getShortDesc() {
            return "multipartFile(File, contentType?) - add multipart file (POST only) - returns self";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() < 1 || params.size() > 2) throw new Exception("Expected File, contentType? parameter(s)");
            Obj x=getObj("File", params, 0);
            String contentType="application/octet-stream";

            if (params.size()==2) {
                contentType=getString("contentType", params, 1);
            }
            if (x instanceof ObjFile) {
                File file=((ObjFile) x).getFile();

                fileUploads.add(new FileUpload(file, contentType));
            } else {
                throw new Exception("Invalid File parameter");
            }
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

        if (url==null) throw new Exception("No url");
        if (method==null) throw new Exception("No method");

        log(ctx,"---- executeCall() ----");

        final String JSON = "application/json";

        HttpURLConnection connection=null;
        try {
            log(ctx,"Creating connection to " + url.toString());
            connection = (HttpURLConnection) (new URL(url)).openConnection();

            log(ctx,"Setting method=" + method);
            connection.setRequestMethod(method);

            if (authString != null) {
                log(ctx, "Setting Authorization: " + authString);
                connection.setRequestProperty("Authorization", authString);
            }

            log(ctx, "Setting Accept: " + JSON);
            connection.setRequestProperty("Accept", JSON);

            // sanity check
            if (jsonData != null && fileUploads.size() > 0) {
                throw new Exception(getDesc() + ": Can not combine jsonData with multipartFile(s)");
            }
            if (fileUploads.size() > 0 && !method.equalsIgnoreCase("post")) {
                throw new Exception(getDesc() + ": The multipartFile(s) require method POST");
            }
            if (jsonData != null) {
                log(ctx,"Setting Content-Type: " + JSON);
                connection.setRequestProperty("Content-Type", JSON);

                connection.setDoOutput(true);
                log(ctx,"Writing jsonData: " + jsonData);
                // Write the JSON data to the request body
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
            } else if (fileUploads.size() > 0 && method.equalsIgnoreCase("post")) {
                final String boundary="----------994820020030051899";

                connection.setDoOutput(true);
                String mpart="multipart/form-data; boundary=" + boundary;
                log(ctx, "Setting Content-Type: " + mpart);
                connection.setRequestProperty("Content-Type", mpart);

                OutputStream out=connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

                for (FileUpload fileUpload : fileUploads) {
                    writer.append("--").append(boundary).append("\r\n");

                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileUpload.file.getName()).append("\"\r\n");
                    writer.append("Content-Type: ").append(fileUpload.contentType).append("\r\n\r\n");
                    writer.flush();

                    FileInputStream fis=new FileInputStream(fileUpload.file);

                    byte[] buf = new byte[8192];
                    for (;;) {
                        int count=fis.read(buf);
                        if (count < 0) break;
                        out.write(buf, 0, count);
                    }
                    out.flush();

                    String md5 = hashMd5(fileUpload.file);
                    writer.append("\r\n--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"checksum\"\r\n\r\n");
                    writer.append(md5);
                    writer.flush();
                }
                // terminate multipart
                writer.append("\r\n--").append(boundary).append("--\r\n");
                writer.close();


            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            dict.set("responseCode", new ValueInt(responseCode));
            log(ctx,"Got responseCode=" + responseCode);

            // Read the response body
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            dict.set("result", new ValueString(sb.toString()));
            log(ctx,"Got result: " + sb.toString());

            return dict;
        } finally {
            if (connection != null) try {connection.disconnect();} catch (Exception ex)  {};
        }

    }

    private static String hashMd5 (File file) throws Exception {
        FileInputStream fis = null;

        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] buf = new byte[4096];
        try {
            fis = new FileInputStream(file);
            for (; ; ) {
                int count = fis.read(buf);
                if (count <= 0) break;
                digest.update(buf, 0, count);
            }
        } finally {
            if (fis != null) try {
                fis.close();
            } catch (Exception ex) {
            }
            ;
        }

        byte[] hash = digest.digest();

        String digits = "0123456789abcdef";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            byte b = hash[i];
            sb.append(digits.charAt((b >> 4) & 0x0F));
            sb.append(digits.charAt(b & 0x0F));
        }
        return sb.toString();
    }
}


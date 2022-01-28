/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

package rf.configtool.main.runtime.lib.integrations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.SentryStackFrame;
import io.sentry.protocol.SentryStackTrace;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDict;

public class ObjSentry extends Obj {

    private boolean initOk=false;
    
    private SentryLevel sentryLevel=SentryLevel.DEBUG;
    
    
    public ObjSentry () {       
        this.add(new FunctionInit());
        
        this.add(new FunctionLevelInfo());
        this.add(new FunctionLevelDebug());
        this.add(new FunctionLevelError());
                
        this.add(new FunctionSendEvent());
        this.add(new FunctionSendBreadCrumb());
        
    }
    
    private ObjSentry self() {
        return this;
    }
    
    @Override
    public boolean eq(Obj x) {
        return false;
    }

    
    @Override
    public String getTypeName() {
        return "Sentry";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("Sentry");
    }
   
    class FunctionInit extends Function {
        public String getName() {
            return "init";
        }
        public String getShortDesc() {
            return "init(DSNString) - initialize Sentry with Data Source Name string - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String dsn=getString("DSNString", params, 0);
            Sentry.init(dsn);

            initOk=true;
            return new ValueObj(self());
        }
    } 
       
    class FunctionLevelInfo extends Function {
        public String getName() {
            return "levelInfo";
        }
        public String getShortDesc() {
            return "levelInfo() - set SentryLevel - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            sentryLevel=SentryLevel.INFO;
            return new ValueObj(self());
        }
    } 
       
    class FunctionLevelDebug extends Function {
        public String getName() {
            return "levelDebug";
        }
        public String getShortDesc() {
            return "levelDebug() - set SentryLevel - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            sentryLevel=SentryLevel.DEBUG;
            return new ValueObj(self());
        }
    } 
       
    
    class FunctionLevelError extends Function {
        public String getName() {
            return "levelError";
        }
        public String getShortDesc() {
            return "levelError() - set SentryLevel - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            sentryLevel=SentryLevel.ERROR;
            return new ValueObj(self());
        }
    } 
       
  
    
    private Map<String,Object> createMap (ObjDict data) {
        // Create proper map
        HashMap<String,Object> map=new HashMap<String,Object>();
        Iterator<String> keys = data.getKeys();
        while (keys.hasNext()) {
            String key=keys.next();
            map.put(key, data.getValue(key).getValAsString());
        }
        return map;
    }

    
    private Map<String,String> createStringMap (ObjDict data) {
        // Create proper map
        HashMap<String,String> map=new HashMap<String,String>();

        Iterator<String> keys = data.getKeys();
        while (keys.hasNext()) {
            String key=keys.next();
            map.put(key, data.getValue(key).getValAsString());
        }
        return map;
    }

    
    class FunctionSendEvent extends Function {
        public String getName() {
            return "sendEvent";
        }
        public String getShortDesc() {
            return "sendEvent(message?, breadCrumbStr?, dataDict?, extraDict?, tagsDict?, exceptionMsg?) - optional params must be null - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (!initOk) throw new Exception("Must call init() first");
            
            if (params.size() != 6) {
                throw new Exception("Expected params: message?, breadCrumbStr?, dataDict?, extraDict?, tagsDict?, exceptionMsg?");
            }
            
            String message=null;
            String breadCrumb=null;
            ObjDict dataDict=null;
            ObjDict extraDict=null;
            ObjDict tagsDict=null;
            String exceptionMsg=null;
            
            
            if (!(params.get(0) instanceof ValueNull)) message=getString("message", params, 0);
            if (!(params.get(1) instanceof ValueNull)) breadCrumb=getString("breadCrumbStr", params, 1);
            if (!(params.get(2) instanceof ValueNull)) dataDict=(ObjDict) getObj("dataDict", params, 2);
            if (!(params.get(3) instanceof ValueNull)) extraDict=(ObjDict) getObj("extraDict", params, 3);
            if (!(params.get(4) instanceof ValueNull)) tagsDict=(ObjDict) getObj("tagsDict", params, 4);
            if (!(params.get(5) instanceof ValueNull)) exceptionMsg=getString("exceptionMsg", params, 5);
  
            SentryEvent event=new SentryEvent();
            
            event.setLevel(sentryLevel);
            
            if (message != null) {      
                Message m=new Message();
                m.setMessage("Got exception: " + message);
                event.setMessage(m);
            }

            if (breadCrumb != null) {
                // https://develop.sentry.dev/sdk/event-payloads/breadcrumbs/
                Breadcrumb bc=new Breadcrumb();
                // bc.setType("default");  
                //bc.setCategory("some-breadcrumb-category");
                bc.setMessage(breadCrumb);
                event.addBreadcrumb(bc);
            }
            
            if (dataDict != null) {
                Map<String,Object> map=createMap(dataDict);
                event.acceptUnknownProperties(map);
            }
            
            if (extraDict != null) {
                Map<String,Object> map=createMap(extraDict);
                event.setExtras(map);
            }
            
            if (tagsDict != null) {
                Map<String,String> map=createStringMap(tagsDict);
                event.setTags(map);
            }
            
            
            
            if (exceptionMsg != null) {
                try {
                    throw new Exception(exceptionMsg);
                } catch (Exception ex) {
                    event.setLevel(SentryLevel.ERROR);
           
                    SentryException sex=new SentryException();
                    sex.setValue(ex.getMessage());
                    
                    List<SentryStackFrame> frames=new ArrayList<SentryStackFrame>();
                    for (StackTraceElement line : ex.getStackTrace()) {
                        
                        SentryStackFrame xx = new SentryStackFrame();
                        xx.setContextLine(line.toString());
                        frames.add(xx);
                    }
                    SentryStackTrace sst=new SentryStackTrace(frames);
                    sex.setStacktrace(sst);
                    
                    List<SentryException> list=new ArrayList<SentryException>();
                    list.add(sex);
                    event.setExceptions(list);
                }
            }
            
            Sentry.captureEvent(event);
            
            return new ValueObj(self());
        }
    } 

    
    class FunctionSendBreadCrumb extends Function {
        public String getName() {
            return "sendBreadCrumb";
        }
        public String getShortDesc() {
            return "sendBreadCrumb (breadCrumbStr) - send breadcrumb to Sentry - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (!initOk) throw new Exception("Must call init() first");
            
            if (params.size() != 1) {
                throw new Exception("Expected params: breadCrumbStr");
            }
            
            String breadCrumb=null;
            
            breadCrumb=getString("breadCrumbStr", params, 0);
  
            // https://develop.sentry.dev/sdk/event-payloads/breadcrumbs/
            Breadcrumb bc=new Breadcrumb();
            bc.setType("default");  
            bc.setCategory("logging");
            bc.setMessage(breadCrumb);

            Sentry.addBreadcrumb(breadCrumb);
            
            return new ValueObj(self());
        }
    } 

    
    
}

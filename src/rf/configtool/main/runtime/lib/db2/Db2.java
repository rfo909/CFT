package rf.configtool.main.runtime.lib.db2;

import java.util.*;

import rf.configtool.main.PropsFile;

import java.io.*;

/**
 * A CFT object storage is a map of strings, which represent code, synthesized from data. 
 */
public class Db2 {
	
	private static Db2 inst;
	
	public synchronized static Db2 getInstance() {
		if (inst==null) inst=new Db2();
		return inst;
	}
	
	
	private String db2Dir;
	
	private Db2() {
		try {
			this.db2Dir = (new PropsFile()).getDb2Dir();
			File f=new File(this.db2Dir);
			f.mkdirs();
		} catch (Exception ex) {
			this.db2Dir="Db2";
		}
	}
	
	private HashMap<String,Map<String,String>> data=new HashMap<String,Map<String,String>>();
	
	private Map<String,String> getCollectionMap (String collection) throws Exception {
		Map<String,String> cMap=data.get(collection);
		if (cMap==null) {
			cMap=initializeFromFile(collection);
			data.put("collection", cMap);
		}
		return cMap;
	}
	
	
	private File getCollectionFile (String collection) {
		return new File(db2Dir+File.separator+"LibDb2_"+collection+".txt");
	}
	
	
	private Map<String,String> initializeFromFile(String collection) throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		File f=getCollectionFile(collection);
		if (!f.exists()) return map;
		
		BufferedReader br=new BufferedReader(new FileReader(f));
		try {
			for (;;) {
				String line=br.readLine();
				if (line==null) break;
				if (line.length()==0) continue;
				int pos=line.indexOf(' ');
				map.put(line.substring(0,pos), line.substring(pos+1));
			}
		} finally {
			if (br != null) try {br.close();} catch (Exception ex) {};
		}
		return map;
	}
	
	private void addToFile (String collection, String key, String value) throws Exception {
		File f=getCollectionFile(collection);
		PrintStream ps = new PrintStream (new FileOutputStream(f, true));
		try {
			ps.println(key + " " + value);
		} finally {
			if (ps != null) try {ps.close();} catch (Exception ex) {};
		}
	}
	
	
	public synchronized void set (String collection, String key, String value) throws Exception {
		if (collection.contains("\r") || collection.contains("\n") || collection.contains(" ")) throw new Exception("Invalid collection name: \\r\\n + space forbidden");
		if (key.contains("\r") || key.contains("\n") || key.contains(" ")) throw new Exception("Invalid key: \\r\\n + space forbidden");
		if (value.contains("\r") || value.contains("\n")) throw new Exception("Invalid value: \\r\\n forbidden");
		
		Map<String,String> cMap=getCollectionMap(collection);  // ensure it is read from file
		addToFile(collection, key, value);
		cMap.put(key,value);
	}
	
	public synchronized String get (String collection, String key) throws Exception {
		return getCollectionMap(collection).get(key);
	}

	public synchronized List<String> getKeys (String collection) throws Exception {
		List<String> list=new ArrayList<String>();
		Iterator<String> iter = getCollectionMap(collection).keySet().iterator();
		while (iter.hasNext()) list.add(iter.next());
		return list;
	}

}

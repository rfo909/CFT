package rf.configtool.root;

import java.util.*;

import rf.configtool.main.runtime.lib.ObjProcess;

/**
 * System object owned by Root
 * @author roar
 *
 */
public class BackgroundProcesses {
	
	class Process {
		private final String name;
		private ObjProcess process;
		
		public Process(String name, ObjProcess process) {
			this.name = name;
			this.process = process;
		}

		public String getName() {
			return name;
		}

		public ObjProcess getProcess() {
			return process;
		}
		
	}
	
	private List<Process> processes=new ArrayList<Process>();
	
	public void add (String name, ObjProcess proc) {
		Process process=new Process(name, proc);
		processes.add(process);
	}
	
	public List<String> getNames () {
		List<String> names=new ArrayList<String>();
		for (Process p:processes) {
			String name=p.getName();
			if (!names.contains(name)) names.add(name);
		}
		return names;
	}
	
	public List<ObjProcess> getProcesses (String name) {
		List<ObjProcess> result=new ArrayList<ObjProcess>();
		for (Process p:processes) {
			if (p.getName().equals(name)) result.add(p.getProcess());
		}
		return result;
	}
	
	public void deleteProcesses (String name) {
		List<Process> toDelete = new ArrayList<Process>();
		for (Process p:processes) {
			if (p.getName().equals(name)) toDelete.add(p);
		}
		for (Process p:toDelete) processes.remove(p);
	}
	
}

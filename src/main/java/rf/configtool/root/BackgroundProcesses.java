/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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
    
    private List<Process> running=new ArrayList<Process>();
    private List<Process> completed=new ArrayList<Process>();
    
    public synchronized void add (String name, ObjProcess proc) {
        checkCompleted();
        Process process=new Process(name, proc);
        running.add(process);
    }
    
    public synchronized List<String> getNamesRunning () {
        checkCompleted();
        List<String> names=new ArrayList<String>();
        for (Process p:running) {
            String name=p.getName();
            names.add(name);
        }
        return names;
    }
    
    public synchronized List<String> getNamesCompleted () {
        checkCompleted();
        List<String> names=new ArrayList<String>();
        for (Process p:completed) {
            String name=p.getName();
            names.add(name);
        }
        return names;
    }
    
    /**
     * Return first running process by name or null if not found
     */
    public synchronized ObjProcess getRunningProcess (String name) {
        checkCompleted();
        for (Process p:running) {
            if (p.getName().equals(name)) return(p.getProcess());
        }
        return null;
    }
    
    /**
     * Return first completed process by name or null if not found
     */
    public synchronized ObjProcess getCompletedProcess (String name) {
        checkCompleted();
        for (Process p:completed) {
            if (p.getName().equals(name)) return(p.getProcess());
        }
        return null;
    }
    
    public synchronized List<String> getCompletedNames () {
        checkCompleted();
        List<String> result=new ArrayList<String>(); 
        for (Process p:completed) {
            result.add(p.getName());
        }
        return result;
    }
    
    public synchronized void deleteCompletedProcesses (String name) {
        checkCompleted();
        List<Process> toDelete = new ArrayList<Process>();
        for (Process p:completed) {
            if (p.getName().equals(name)) toDelete.add(p);
        }
        for (Process p:toDelete) completed.remove(p);
    }
    
    private synchronized void checkCompleted () {
        List<Process> toMove = new ArrayList<Process>();
        for (Process p:running) {
            if (!p.getProcess().isAlive()) {
                toMove.add(p);
            }
        }
        for (Process p:toMove) {
            running.remove(p);
            completed.add(p);
        }
    }
}

package rf.configtool.main;

public class LastExtProgramStatus {

	private int status;
	private String command;
	private Long duration;
	
	public LastExtProgramStatus(int status, String command, long duration) {
		this.status = status;
		this.command = command;
		this.duration = duration;
	}

	public int getStatus() {
		return status;
	}

	public String getCommand() {
		return command;
	}
	
	public Long getDuration() {
		return duration;
	}
	

	
}

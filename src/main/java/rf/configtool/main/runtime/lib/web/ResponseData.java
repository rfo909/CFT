package rf.configtool.main.runtime.lib.web;

public class ResponseData {

	private String contentType;
	private byte[] data;
	
	public ResponseData (byte[] data) {
		this("text/html", data);
	}
	
	public ResponseData(String contentType, byte[] data) {
		super();
		this.contentType = contentType;
		this.data = data;
	}

	public String getContentType() {
		return contentType;
	}

	public byte[] getData() {
		return data;
	}


}


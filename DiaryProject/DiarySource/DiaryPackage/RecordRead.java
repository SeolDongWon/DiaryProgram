package DiaryPackage;

public class RecordRead {

	private String id;
	private String data;
	private String content;

	public RecordRead(String id, String data, String content) {
		super();
		this.id = id;
		this.data = data;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
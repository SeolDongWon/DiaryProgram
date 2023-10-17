package DiaryPackage;

public class Account {
	private String id;
	private String pass;
	private String type;
	private String authority;
	private String call;
	private String address;

	public Account() {
		super();
	}

	public Account(String id, String pass, String type, String authority, String call, String address) {
		super();
		this.id = id;
		this.pass = pass;
		this.type = type;
		this.authority = authority;
		this.call = call;
		this.address = address;
	}

	public String getId() {
		return id;
	}

	public String getPass() {
		return pass;
	}

	public String getType() {
		return type;
	}

	public String getAuthority() {
		return authority;
	}

	public String getCall() {
		return call;
	}

	public String getAddress() {
		return address;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public void setCall(String call) {
		this.call = call;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
package eu.dillendapp.xmas.xmas_remote;

public class XmasMode {

	private String name;
	private int mode_no;
	private boolean allow_static_setting;
	
	XmasMode(String name, int mode_no, boolean allow_static_setting) {
		this.name = name;
		this.mode_no = mode_no;
		this.allow_static_setting = allow_static_setting;
	}
	
	public String getName() {
		return name;
	}

	public int getMode_no() {
		return mode_no;
	}
	
	public boolean getAllow_static_setting() {
		return allow_static_setting;
	}

	@Override
	public String toString() {
		return name;
	}
	
}

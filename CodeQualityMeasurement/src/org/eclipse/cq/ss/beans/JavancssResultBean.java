package org.eclipse.cq.ss.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "javancss")
public class JavancssResultBean {

	String date;
	String time;
	Functions functions;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Functions getFunctions() {
		return functions;
	}

	public void setFunctions(Functions functions) {
		this.functions = functions;
	}

}

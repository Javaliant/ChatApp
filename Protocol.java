/* Author: Luigi Vincent */

public enum Protocol {
	SUBMIT,
	RESUBMIT,
	ACCEPT,
	CONNECT,
	DISCONNECT,
	INFORM,
	MESSAGE;

	public int index() {
		return name().length();
	}
}
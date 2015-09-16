/* Author: Luigi Vincent
* enum to handle Server-Client I/O
*/

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
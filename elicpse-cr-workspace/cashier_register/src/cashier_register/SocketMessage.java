package cashier_register;

public class SocketMessage {
	public String type;
	public int intValues[];
	public String strValues[];
	
	public SocketMessage(String type, int[] intValues, String[] strValues) 	{
		this.type = type;
		this.intValues = intValues;
		this.strValues = strValues;
	}
}

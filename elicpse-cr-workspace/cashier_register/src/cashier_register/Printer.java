package cashier_register;

public class Printer {
	public String receipt;
	public String Check;
	public Client client;
	
	public Printer(Client client) {
		this.client = client;
	}
	
	public void printReceipt(String receipt) {
		 client.resp.add("print-receipt");
	}
	
	public Check modifyCheck(Check check, String additionalInfo) {
		check.additionalInfo = additionalInfo;
		return check;
	}
}

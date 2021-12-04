package cashier_register;

import java.time.*;

public class Check {
	public String fullname;
	public String accountNumber;
	public LocalDate date;
	public String additionalInfo;
	
	public Check(String name, String account, String additionalInfo) {
		this.date = LocalDate.now();
		this.accountNumber = account;
		this.fullname = name;
		this.additionalInfo = additionalInfo;
	}
}

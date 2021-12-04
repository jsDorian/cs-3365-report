package cashier_register;

public class Register {
	public int cash = 0;
	public boolean drawerOpen = false;
	
	public void addCash(int amount) {
		if (drawerOpen) {
			this.cash += amount;
		}
	}
	
	public int getChange(int change) {
		if (drawerOpen) {
			this.cash -= change;
			return change;
		} else {
			return -1;
		}
	}
	
	public void openDrawer() {
		this.drawerOpen = true;
	}
	
	public void closeDrawer() {
		this.drawerOpen = false;
	}
}

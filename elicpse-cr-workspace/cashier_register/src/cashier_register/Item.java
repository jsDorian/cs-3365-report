package cashier_register;

public class Item {
	public String name;
	public int price;
	public int amount;
	public int bulk;
	public int weight;
	public int id;
	
	public Item(String name, int price, int amount, int weight, int bulk) {
		this.name = name;
		this.price = price;
		this.amount = amount;
		this.weight = weight;
		this.bulk = bulk;
	}
	
	public Item(String name, int price, int amount, int weight, int bulk, int id) {
		this.name = name;
		this.price = price;
		this.amount = amount;
		this.weight = weight;
		this.bulk = bulk;
		this.id = id;
	}
}

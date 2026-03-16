package Raspberry.model;

public class User {
	private int id;
	private String username;
	private String rfidTag;
	private double balance;
	private boolean active;
	private String role;


	public User() {
		this.active = true;
		this.balance = 0.0;

	}

	public User(String username, String rfidTag, double balance,  String role) {
		this.username = username;
		this.rfidTag = rfidTag;
		this.balance = balance;
		this.active = true;
		this.role = role;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRfidTag() {
		return rfidTag;
	}

	public void setRfidTag(String rfidTag) {
		this.rfidTag = rfidTag;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}

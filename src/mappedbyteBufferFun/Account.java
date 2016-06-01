package mappedbyteBufferFun;

import java.io.Serializable;

public class Account implements Serializable {

	private int id;
	private String name;
	private String surname;
	private String PESEL;
	private String address;
	private float balance;
	
	Account (int idd, String nam, String surnam, String pesel, String addr) {
		id=idd;
		name = nam;
		surname = surnam;
		PESEL=pesel;
		address=addr;
		balance = 0f;
	}
	
	public String toString() {	
		return "ID: " + Integer.toString(id)+"\nName:  " + name+" "+surname + "\nPESEL: " + PESEL +
				"\nAddress: " + address + "\nBalance: " + Float.toString(balance) + "$";
	}
	
	public void setBalance(Float f) {
		balance = f;
	}
	
	public int getId(){
		return id;
	}
	public String getName(){
		return name;
	}
	public String getSecondName(){
		return surname;
	}
	public String getPesel(){
		return PESEL;
	}
	public String getAddress(){
		return address;
	}
	public float getBalance(){
		return balance;
	}
}

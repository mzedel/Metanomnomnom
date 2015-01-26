package de.hpi.dpdc.dubstep.detection.address;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class Address {
	
	public Integer origId;

	public String title;

	public String firstName;

	public String lastName;

	public String birthYear;

	public String birthMonth;

	public String birthDay;

	public Date birthDate;

	public String streetName;

	public String houseNumber;

	public String postalCode;

	public String city;

	public String phoneNumber;

	public String key;

	public Address() {
		super();
	}

	public Address(String[] rawAdress) {
		this();
		
		// copy values
		this.origId = Integer.parseInt(rawAdress[0]);	// must not fail
		this.title = rawAdress[2];
		this.firstName = rawAdress[3];
		this.lastName = rawAdress[4];
		this.birthYear = rawAdress[5];
		this.birthMonth = rawAdress[6];
		this.birthDay = rawAdress[7];
		// construct full date: yyyy.mm.dd
		try {
			this.birthDate = DateFormat.getInstance().parse(this.birthYear + '.' + this.birthMonth + '.' + this.birthDay + '.');
		} catch (ParseException e) {
			this.birthDate = null;
		}
		this.streetName = rawAdress[8];
		this.houseNumber = rawAdress[9];
		this.postalCode = rawAdress[10];
		this.city = rawAdress[11];
		this.phoneNumber = rawAdress[12];
		// create key for blocking
		if (this.postalCode != null && this.lastName != null) {
			this.key = this.postalCode.substring(0, Math.min(this.postalCode.length(), 3))
					+ ":"
					+ this.lastName.substring(0, Math.min(this.lastName.length(), 3));
		} // else: key remains null
	}
	
	private final String seperator = "|";
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(this.origId);
	    sb.append(seperator);
	    sb.append(this.title);
	    sb.append(seperator);
	    sb.append(this.firstName);
	    sb.append(seperator);
	    sb.append(this.lastName);
	    sb.append(seperator);
	    sb.append(this.city);
	    sb.append(seperator);
	    sb.append(this.houseNumber);
	    sb.append(seperator);
	    sb.append(this.birthYear);
	    sb.append(seperator);
	    sb.append(this.postalCode);
	    return sb.toString();
	}
}

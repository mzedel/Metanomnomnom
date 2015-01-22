package de.hpi.dpdc.dubstep.detection.address;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "adresses",
       indexes = {@Index(name = "last_name_index",  columnList="LastName", unique = false),
                  @Index(name = "first_name_index",  columnList="FirstName", unique = false),
                  @Index(name = "key_index",  columnList="Key", unique = false)})
public class Address {
  
  // do not change the ID
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Integer id;
  
  @Column(name="OrigId", length=20, nullable=true)
  public String OrigId;
    
  public String Title;
  
  public String FirstName;
  
  public String LastName;
  
  public String BirthYear;
  
  public String BirthMonth;
  
  public String BirthDay;
  
  public Date BirthDate;
  
  public String StreetName;
  
  public String HouseNumber;
  
  public String PostalCode;
  
  public String City;
  
  public String PhoneNumber;
  
  public int SetAttributeCount = 0;
  
  public String Key;
  
  public Address() {
    super();
  }
  
  public Address(String[] rawAdress) {
    this();
    this.OrigId = rawAdress[0];
    this.Title = rawAdress[2];
    this.FirstName = rawAdress[3];
    this.LastName = rawAdress[4];
    this.BirthYear = rawAdress[5];
    this.BirthMonth = rawAdress[6];
    this.BirthDay = rawAdress[7];
    try {
      this.BirthDate = DateFormat.getInstance().parse(this.BirthYear + '.' + this.BirthMonth + '.' + this.BirthDay + '.');
    } catch (ParseException e) {
      this.BirthDate = null;
    }
    this.StreetName = rawAdress[8];
    this.HouseNumber = rawAdress[9];
    this.PostalCode = rawAdress[10];
    this.City = rawAdress[11];
    this.PhoneNumber = rawAdress[12];
    for (String item : rawAdress) {
      if(item != null && !item.isEmpty())
        this.SetAttributeCount++;
    }
    if (this.PostalCode != null && this.LastName != null) {
	this.Key = this.PostalCode.substring(0, Math.min(this.PostalCode.length(), 3))
		+ ":"
    		+ this.LastName.substring(0, Math.min(this.LastName.length(), 2));
    }
  }
}

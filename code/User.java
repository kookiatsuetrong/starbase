/**
 * User detail
 * 
 */
public class User {
	long number;
	String email;
	String password;
	String firstName;
	String lastName;
	String status;
	String signature;
	long   identifier;
	String theme;
	String language;
	
	public long   getNumber()     { return number;     }
	public String getEmail()      { return email;      }
	public String getFirstName()  { return firstName;  }
	public String getLastName()   { return lastName;   }
	public String getStatus()     { return status;     }
	public String getSignature()  { return signature;  }
	public long   getIdentifier() { return identifier; }
	public String getTheme()      { return theme;      }
	public String getLanguage()   { return language;   }
	
	boolean administrator;
	boolean staff;
	
	public boolean isAdministrator() { return administrator; }
	public boolean isStaff()         { return staff;         }
	
}

/**
 * Password validation
 */

import java.util.ArrayList;

class PasswordDetail {
	public static final int OK                     = 0b00000;
	public static final int TOO_SHORT              = 0b00001;
	public static final int TOO_LONG               = 0b00010;
	public static final int AT_LEAST_ONE_UPPERCASE = 0b00100;
	public static final int AT_LEAST_ONE_LOWERCASE = 0b01000;
	public static final int AT_LEAST_ONE_DIGIT     = 0b10000;
	
	public static int check(String password) {
		if (password == null) return PasswordDetail.TOO_SHORT;
		
		int detail = PasswordDetail.OK;
		if (password.length() <  8) {
			detail += PasswordDetail.TOO_SHORT;
		}
		if (password.length() > 60) {
			detail += PasswordDetail.TOO_LONG;
		}
		if (password.matches(".*[0-9].*") == false) {
			detail += PasswordDetail.AT_LEAST_ONE_DIGIT;
		}
		if (password.matches(".*[a-z].*") == false) {
			detail += PasswordDetail.AT_LEAST_ONE_LOWERCASE;
		}
		if (password.matches(".*[A-Z].*") == false) {
			detail += PasswordDetail.AT_LEAST_ONE_UPPERCASE;
		}
		return detail;
	}
	
	/*
	public static ArrayList<String> find(int detail) {
		ArrayList<String> result = new ArrayList<>();
		
		if ((detail & TOO_SHORT) == TOO_SHORT) {
			result.add("Password length must be at least 8 characters.");
		}
		if ((detail & TOO_LONG) == TOO_LONG) {
			result.add("Password is too long.");
		}
		if ((detail & AT_LEAST_ONE_DIGIT) == AT_LEAST_ONE_DIGIT) {
			result.add("Password must contains at least one digit.");
		}
		if ((detail & AT_LEAST_ONE_UPPERCASE) == AT_LEAST_ONE_UPPERCASE) {
			result.add("Password must contains at least one uppercase.");
		}
		if ((detail & AT_LEAST_ONE_LOWERCASE) == AT_LEAST_ONE_LOWERCASE) {
			result.add("Password must contains at least one lowercase.");
		}
		
		return result;
	}
	*/
	
	/*
	password-too-long
	password-too-short
	password-has-no-digit
	password-has-no-uppercase
	password-has-no-lowercase
	*/
	
	public static ArrayList<String> listError(int detail) {
		ArrayList<String> result = new ArrayList<>();
		
		if ((detail & TOO_SHORT) == TOO_SHORT) {
			result.add("password-too-short");
		}
		if ((detail & TOO_LONG) == TOO_LONG) {
			result.add("password-too-long");
		}
		if ((detail & AT_LEAST_ONE_DIGIT) == AT_LEAST_ONE_DIGIT) {
			result.add("password-has-no-digit");
		}
		if ((detail & AT_LEAST_ONE_UPPERCASE) == AT_LEAST_ONE_UPPERCASE) {
			result.add("password-has-no-uppercase");
		}
		if ((detail & AT_LEAST_ONE_LOWERCASE) == AT_LEAST_ONE_LOWERCASE) {
			result.add("password-has-no-lowercase");
		}
		
		return result;
	}
	
}

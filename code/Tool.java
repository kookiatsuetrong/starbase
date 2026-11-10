/**
 * The common subprograms
 * 
 */
import java.io.FileReader;
import java.util.TreeMap;

public class Tool {
	
	public static String 
	createPhotoCode(final String text) {
		String name = "photo-code/" + text + ".txt";
		String buffer = "";
		try {
			FileReader fr = new FileReader(name);
			while (true) {
				int k = fr.read();
				if (k == -1) break;
				buffer += (char)k;
			}
		} catch (Exception e) { 
			System.out.println("File not found " + name);
		}
		return buffer;
	}
	
	public static String 
	randomPhotoCode() {
		int r = (int)(Math.random() * 10000);
		String s = "" + r;
		while (s.length() < 4) { s = "0" + s; }
		return s;
	}
	
	public static String 
	randomActivationCode() {
		int r = (int)(Math.random() * 1000000);
		String s = "" + r;
		while (s.length() < 6) { s = "0" + s; }
		return s;
	}
	
	public static String 
	randomCode(int size) {
		char[] a = "0123456789".toCharArray();
		String buffer = "";
		for (int i = 0; i < size; i++) {
			int r = (int)(Math.random() * a.length);
			buffer += a[r];
		}
		return buffer;
	}
	
	public static long
	getLongOrZero(String data) {
		long value = 0;
		try {
			value = Long.valueOf(data);
		} catch (Exception e) { }
		return value;
	}
	
	public static double
	getDoubleOrZero(String data) {
		double value = 0.0;
		try {
			value = Double.valueOf(data);
		} catch (Exception e) { }
		return value;
	}
	
	public static TreeMap<String, Object> 
	createSuccess(String reason) {
		TreeMap<String, Object> result = new TreeMap<>();
		result.put("result", "OK");
		result.put("reason", reason);
		return result;
	}
	
	public static TreeMap<String, Object> 
	createError(String reason) {
		TreeMap<String, Object> result = new TreeMap<>();
		result.put("result", "ERROR");
		result.put("reason", reason);
		return result;
	}
	
	//                   01 234 567 890 123 456 789
	// From long number: aa,bbb,ccc,ddd,eee,fff,ggg
	//                      ddd,eee,_f_,f_f,_g_,g_g
	//                    1_123_456_789_123_456_789
	//                      789_123_x4x_5x6_x7x_8x9

	public static long
	createIdentifier(long number) {
		String s = "" + number;
		while (s.length() < 20) {
			s = "0" + s;
		}
		
		char[] a = s.toCharArray();
		
		a[ 0] = '0';
		a[ 1] = '1';
		
		a[ 2] = a[ 8];
		a[ 3] = a[ 9];
		a[ 4] = a[10];
		
		a[ 5] = a[11];
		a[ 6] = a[12];
		a[ 7] = a[13];
		
		a[ 8] = (char)('0' + Math.random() * 10);
		a[ 9] = a[14];
		a[10] = (char)('0' + Math.random() * 10);
		a[11] = a[15];
		a[12] = (char)('0' + Math.random() * 10);
		a[13] = a[16];
		a[14] = (char)('0' + Math.random() * 10);
		a[15] = a[17];
		a[16] = (char)('0' + Math.random() * 10);
		a[17] = a[18];
		a[18] = (char)('0' + Math.random() * 10);
		
		String buffer = "";
		for (int i = 0; i < a.length; i++) {
			buffer += a[i];
		}
		
		long value = number;
		try {
			value = Long.parseLong(buffer);
		} catch (Exception e) { }
		
		System.out.println("Input  " + s);
		System.out.println("Buffer " + buffer);
		System.out.printf ("Output %20d\n", value);
		System.out.println("               ^ ^ ^ ^ ^ ^");
		
		return value;
	}
}
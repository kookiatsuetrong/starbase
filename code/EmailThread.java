import java.sql.DriverManager;

class EmailThread extends Thread {
	
	@Override public void run() {
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (Exception e) { }
			
			try {
				String s =  " select * from emails     " +
							" where status = 'created' ";

				var cn = DriverManager.getConnection(Storage.source);
				var ps = cn.prepareStatement(s);
				var rs = ps.executeQuery();
				if (rs.next()) {
					long number    = rs.getLong("number");
					String address = rs.getString("address");
					String subject = rs.getString("subject");
					String text    = rs.getString("text");
					String html    = rs.getString("html");
					
					update(number, "preparing");
					System.out.println("Sending email to " + address + 
										" " + text);
					
					EmailSender e = new EmailSender(address, 
											subject, text, html);
					e.start();
					update(number, "sent");
				}
				rs.close();
				ps.close();
				cn.close();
			} catch (Exception e) { }
			
		}
	}
	
	void update(long number, String status) {
		try {
			String s =  " update emails set status = ? " +
						" where number = ?             ";

			var cn = DriverManager.getConnection(Storage.source);
			var ps = cn.prepareStatement(s);
			ps.setString(1, status);
			ps.setLong  (2, number);
			ps.execute();
			ps.close();
			cn.close();
		} catch (Exception e) { }

	}
	
}

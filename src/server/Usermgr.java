package server;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;

public class Usermgr {
	private Statement st;
	private ResultSet rs;

	public Usermgr(Statement state) {
		st = state;
	}

	public int register(InetAddress addr, String[] info) {
		// info[1] contains username
		// info[2] contains password
		try {
			st.execute("INSERT INTO userlist (nickname, passwd, ipaddress, onlinestatus) values('" + info[1] + "','"
					+ info[2] + "','" + addr.getHostAddress().toString() + "',true)");
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
				return 1;
			}
			e.printStackTrace();
		}
		return 0;
	}

	public int login(InetAddress ip, String[] inputs) {
		// inputs[1] contains username
		// inputs[2] contains password
		try {
			rs = st.executeQuery("select * from userlist where nickname = '" + inputs[1] + "'");
			if (!rs.first()) {
				return 1;
			}
			String nameentered = rs.getString("nickname");
			String passentered = rs.getString("passwd");
			st.execute("update userlist set ipaddress = '" + ip.getHostAddress().toString() + "'where nickname = '"
					+ inputs[1] + "'");
			if (inputs[2].equals(passentered) && inputs[1].equals(nameentered)) {
				st.execute("update userlist set onlinestatus = true where nickname = '" + inputs[1] + "'");
				return 0;
			} else if (!inputs[2].equals(passentered)) {
				return 2; // wrong password
			}
			return 3;// unknown
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean logout(InetAddress ip, String username) {
		try {
			st.execute("update userlist set onlinestatus = false where nickname = '" + username + "'");
			// check IP address, if doesn't match, refuse the request to prevent
			// from being maliciously logged out
			rs = st.executeQuery("select * from userlist where nickname = '" + username + "'");
			if (rs.first()) {
				if (rs.getString("ipaddress").equals(ip.getHostAddress())) {
					st.execute("update userlist set ipaddress = '' where nickname = '" + username + "'");
				} else {
					return false;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean remove(String username) {
		try {
			st.execute("delete from userlist where nickname = '" + username + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public LinkedList<HashMap<String, Object>> userlist() throws SQLException {
		LinkedList<HashMap<String, Object>> userls = new LinkedList<HashMap<String, Object>>();

		rs = st.executeQuery("select * from userlist where onlinestatus != false");
		while (rs.next()) {
			HashMap<String, Object> usr = new HashMap<String, Object>();
			usr.put("Name", rs.getString("nickname"));
			usr.put("IP", rs.getString("ipaddress"));
			userls.add(usr);
		}
		return userls;
	}

	public String findIPbyname(String name) {
		try {
			rs = st.executeQuery("select ipaddress from userlist where nickname = '" + name + "'");
			rs.first();
			return rs.getString("ipaddress");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}

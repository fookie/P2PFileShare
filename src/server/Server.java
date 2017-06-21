package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Server {

	public static void main(String[] args) throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/database", "root", "toor");
		Statement st = conn.createStatement();
		st.execute(
				"CREATE TABLE IF NOT EXISTS userlist(nickname char(10) NOT NULL,passwd char(8) NOT NULL,ipaddress char(15) DEFAULT NULL,onlinestatus tinyint(4) NOT NULL DEFAULT '0',PRIMARY KEY (nickname),UNIQUE KEY nickname (nickname))");
		st.execute(
				"CREATE TABLE IF NOT EXISTS filelist(fileid char(32) NOT NULL,filename char(255) NOT NULL,fileowner char(10) DEFAULT NULL,size float NOT NULL,  location TEXT(1000) NOT NULL,  PRIMARY KEY (fileid),  KEY fileowner (fileowner), FOREIGN KEY (fileowner) REFERENCES userlist (nickname) ON DELETE CASCADE ON UPDATE CASCADE)");
		Usermgr usermgr = new Usermgr(st);
		Filemgr filemgr = new Filemgr(st);
		Networkmgr networkmgr = new Networkmgr(usermgr, filemgr);
		networkmgr.initnet();
		st.close();
		conn.close();
	}

}

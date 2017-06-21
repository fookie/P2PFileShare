package server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;

public class Filemgr {
	private Statement st;
	private ResultSet rs;

	public Filemgr(Statement statement) {
		st = statement;
	}

	public int add(String[] inputs) {
		String filename = inputs[1];
		String md5checksum = inputs[2];
		String path = inputs[3];
		String owner = inputs[4];
		long size = Long.parseLong(inputs[5]);
		try {
			System.out.println("Adding file...");
			st.execute("INSERT INTO filelist(fileid, filename, fileowner, size, location) values('" + md5checksum
					+ "','" + filename + "','" + owner + "'," + size + ",'" + path + "')");
			System.out.println("file added");
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
				return 0;
			} else {
				return 2;
			}
		}
		return 1;
	}

	public boolean remove(String md5checksum) {
		try {
			st.execute("delete from filelist where fileid = '" + md5checksum + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public LinkedList<HashMap<String, Object>> filelist() {
		LinkedList<HashMap<String, Object>> filels = new LinkedList<HashMap<String, Object>>();

		try {
			rs = st.executeQuery(
					"select * from filelist where fileowner in (select nickname from userlist where onlinestatus = true)");
			while (rs.next()) {
				HashMap<String, Object> file = new HashMap<String, Object>();
				file.put("fileid", rs.getString("fileid"));
				file.put("filename", rs.getString("filename"));
				file.put("fileowner", rs.getString("fileowner"));
				file.put("size", rs.getLong("size"));
				file.put("location", "location");
				filels.add(file);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return filels;
	}

	public String findLoc(String md5) {
		try {
			rs = st.executeQuery("select location from filelist where fileid = '" + md5 + "'");
			rs.first();
			return rs.getString("location");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public long getSize(String md5) {
		try {
			rs = st.executeQuery("select size from filelist where fileid = '" + md5 + "'");
			rs.first();
			return rs.getLong("size");
		} catch (SQLException e) {
			e.printStackTrace();
			return 1;
		}
	}

}

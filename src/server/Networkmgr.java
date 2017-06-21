package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Networkmgr {
	Usermgr userdb;
	Filemgr filedb;

	public Networkmgr(Usermgr u, Filemgr f) {
		userdb = u;
		filedb = f;
	}

	public void initnet() {
		HandlerThread handler;
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(15810);
			while (true) {
				Socket client = serverSocket.accept();
				handler = new HandlerThread(client);
				Thread thread = new Thread(handler);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Server initialization failure");
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class HandlerThread implements Runnable {

		private Socket socket;
		private String username;
		private InetAddress addr;

		public HandlerThread(Socket client) {
			socket = client;
		}

		public void run() {
			DataInputStream request = null;
			DataOutputStream response = null;
			ObjectOutputStream listSender = null;
			try {
				System.out.println(socket.getInetAddress().getHostName() + " connected");
				int statuscode = -1;// value returned by functions
				// receiving request
				while (true) {
					request = new DataInputStream(socket.getInputStream());
					String clientInput = request.readUTF();
					System.out.println(clientInput);
					// response
					response = new DataOutputStream(socket.getOutputStream());
					// inputs[0] command code
					// inputs[1] username/filename
					// inputs[2] password/md5checksum
					String[] inputs = clientInput.split(" ");
					switch (inputs[0]) {
					case "100":// register
						username = inputs[1];
						addr = socket.getInetAddress();
						statuscode = userdb.register(addr, inputs);
						if (statuscode == 1) {
							// return 001
							response.writeUTF("001");
							response.flush();
						} else if (statuscode == 0) {
							// return 003
							response.writeUTF("003");
							response.flush();
						} else {
							// return 404
							response.writeUTF("404");
							response.flush();
						}
						break;
					case "101":// log in
						statuscode = userdb.login(socket.getInetAddress(), inputs);
						username = inputs[1];
						addr = socket.getInetAddress();
						if (statuscode == 0) {
							// return 020
							response.writeUTF("020");
							response.flush();
						} else if (statuscode == 1) {
							// return 010
							response.writeUTF("010");
							response.flush();
						} else if (statuscode == 2) {
							// return 011
							response.writeUTF("011");
							response.flush();
						} else {
							// return 404
							response.writeUTF("404");
							response.flush();
						}
						break;
					case "102":// log out.
						if (!userdb.logout(socket.getInetAddress(), inputs[1])) {
							response.writeUTF("404");
							response.flush();
						}
						break;
					case "103":// delete current user.
						if (userdb.remove(inputs[1])) {
							// return 000
							response.writeUTF("000");
							response.flush();
						} else {
							response.writeUTF("404");
							response.flush();
						}
						break;
					case "111":// Request for user list.
						// send back
						listSender = new ObjectOutputStream(response);
						listSender.writeObject(userdb.userlist());
						listSender.flush();
						break;
					case "112":// Request for file list.
						listSender = new ObjectOutputStream(response);
						listSender.writeObject(filedb.filelist());
						listSender.flush();
						break;
					case "120":// add file
						statuscode = filedb.add(inputs);
						if (statuscode == 0) {
							// file exists
							response.writeUTF("023");
							response.flush();
						} else if (statuscode == 1) {
							response.writeUTF("021");
							response.flush();
						} else {
							// return 404
							response.writeUTF("404");
							response.flush();
						}
						break;
					case "121":// remove file
						if (filedb.remove(inputs[1])) {
							response.writeUTF("022");
							response.flush();
						} else {
							// return 404
							response.writeUTF("404");
							response.flush();
						}
						break;
					case "130":// ask for address
						response.writeUTF(userdb.findIPbyname(inputs[1]));
						response.flush();
						break;
					case "131":
						response.writeUTF(filedb.findLoc(inputs[1]));
						response.flush();
						break;
					case "132":
						response.writeLong(filedb.getSize(inputs[1]));
						response.flush();
					default:
						// invalid code
						response.writeUTF("404");
						response.flush();
						break;
					}
				}
			} catch (Exception e) {
				userdb.logout(addr, username);
				System.err.println(socket.toString() + " disconnected");
			}
		}

	}

}

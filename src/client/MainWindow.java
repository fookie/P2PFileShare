package client;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.Color;
import javax.swing.table.DefaultTableModel;

import javax.swing.JScrollPane;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -7911273892329200332L;
	private Socket socket = null;
	private String owner;
	private DataInputStream dis;
	private DataOutputStream dos;
	private JTable usertable, filetable;
	private DefaultTableModel usermodel, filemodel;
	private JProgressBar progressBar;
	private ObjectInputStream ois;

	public MainWindow(String user, Socket connection, DataInputStream in, DataOutputStream out) {
		service tHandler = new service();
		Thread thread = new Thread(tHandler);
		thread.start();

		owner = user;
		socket = connection;
		dis = in;
		dos = out;
		setTitle("Filesharer");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 625, 471);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		this.setLocationRelativeTo(null);

		usertable = new JTable();
		usermodel = new DefaultTableModel() {
			private static final long serialVersionUID = -1053317994801719315L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		usermodel.addColumn("User name");
		usermodel.addColumn("IP Address");
		usertable.setModel(usermodel);
		usertable.getTableHeader().setReorderingAllowed(false);
		usertable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane userListScrollPane = new JScrollPane(usertable);
		userListScrollPane.setBounds(10, 41, 155, 326);
		contentPane.add(userListScrollPane);
		refreshUserList();

		filetable = new JTable();
		filemodel = new DefaultTableModel() {
			private static final long serialVersionUID = -6424902787560815682L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		filemodel.addColumn("MD5");
		filemodel.addColumn("Name");
		filemodel.addColumn("Size");
		filemodel.addColumn("Owner");
		filetable.setModel(filemodel);
		filetable.getTableHeader().setReorderingAllowed(false);
		filetable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane fileListScrollPane = new JScrollPane(filetable);
		fileListScrollPane.setBounds(175, 41, 432, 326);
		contentPane.add(fileListScrollPane);
		refreshFileList();

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(10, 377, 599, 25);
		contentPane.add(progressBar);
		progressBar.setValue(0);
		progressBar.setString(progressBar.getValue() + "%");

		JButton btnRefreshUserList = new JButton("Refresh user list");
		btnRefreshUserList.setBounds(10, 4, 155, 27);
		contentPane.add(btnRefreshUserList);

		JButton btnRefreshFileList = new JButton("Refresh file list");
		btnRefreshFileList.setBounds(175, 4, 140, 27);
		contentPane.add(btnRefreshFileList);

		JButton btnAddAFile = new JButton("Add a file");
		btnAddAFile.setBounds(10, 412, 222, 27);
		contentPane.add(btnAddAFile);

		JButton btnRemoveAFile = new JButton("Remove a file");
		btnRemoveAFile.setBounds(242, 412, 183, 27);
		contentPane.add(btnRemoveAFile);

		JButton btnDeleteThisUser = new JButton("Delete this account");
		btnDeleteThisUser.setForeground(Color.RED);
		btnDeleteThisUser.setBounds(325, 4, 155, 27);
		contentPane.add(btnDeleteThisUser);

		JButton btnLogout = new JButton("Logout");
		btnLogout.setBounds(490, 4, 117, 27);
		contentPane.add(btnLogout);

		JButton btnDownloadTheFile = new JButton("Download the file");
		btnDownloadTheFile.setBounds(435, 412, 172, 27);
		contentPane.add(btnDownloadTheFile);

		MouseListener mListener = new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getComponent().equals(btnAddAFile)) {
					addFile();
				} else if (e.getComponent().equals(btnDeleteThisUser)) {
					if (JOptionPane.showConfirmDialog(null, "Are you sure? This can't be undo", "Confirm?",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						removeThisAccount();
					}
				} else if (e.getComponent().equals(btnDownloadTheFile)) {
					if (filetable.getSelectedRow() != -1) {
						JFileChooser save = new JFileChooser();
						save.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						save.showSaveDialog(null);
						try {
							dos.writeUTF("130 " + filetable.getValueAt(filetable.getSelectedRow(), 3).toString());
							String filehost = dis.readUTF();
							dos.writeUTF("131 " + filetable.getValueAt(filetable.getSelectedRow(), 0).toString());
							String filepath = dis.readUTF();
							dos.writeUTF("132 " + filetable.getValueAt(filetable.getSelectedRow(), 0).toString());
							long filesize = dis.readLong();
							fetch fetchthread = new fetch(filehost,
									(String) filetable.getValueAt(filetable.getSelectedRow(), 1), filepath,
									(String) filetable.getValueAt(filetable.getSelectedRow(), 0), filesize,
									save.getSelectedFile().getAbsolutePath());
							Thread fetchprocess = new Thread(fetchthread);
							fetchprocess.start();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(null, "No file was selected");
					}
				} else if (e.getComponent().equals(btnLogout)) {
					logout();
				} else if (e.getComponent().equals(btnRefreshFileList)) {
					refreshFileList();
				} else if (e.getComponent().equals(btnRefreshUserList)) {
					refreshUserList();
				} else if (e.getComponent().equals(btnRemoveAFile)) {
					// 121 to server
					if (filetable.getSelectedRow() != -1) {
						removeFile((String) filetable.getValueAt(filetable.getSelectedRow(), 0),
								(String) filetable.getValueAt(filetable.getSelectedRow(), 3));
					} else {
						JOptionPane.showMessageDialog(null, "No file has been selected");
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		};

		btnAddAFile.addMouseListener(mListener);
		btnDeleteThisUser.addMouseListener(mListener);
		btnDownloadTheFile.addMouseListener(mListener);
		btnLogout.addMouseListener(mListener);
		btnRefreshFileList.addMouseListener(mListener);
		btnRefreshUserList.addMouseListener(mListener);
		btnRemoveAFile.addMouseListener(mListener);

	}

	private void addFile() {
		JFileChooser jfc = new JFileChooser();
		jfc.showDialog(null, "Select");
		File selectedFile = jfc.getSelectedFile();
		if (selectedFile != null) {
			String filename = selectedFile.getName();
			String md5 = "empty";
			System.out.println("Adding");
			try {
				md5 = getMD5Checksum(selectedFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			long filesize = selectedFile.length();
			String path = selectedFile.getPath();
			path = path.replace('\\', '/');
			System.out.println("Getting MD5");

			try {
				dos.writeUTF("120 " + filename + " " + md5 + " " + path + " " + owner + " " + filesize);
				String response = dis.readUTF();
				if (response.equals("021")) {
					JOptionPane.showMessageDialog(null, "File added");
					refreshFileList();
				} else if (response.equals("023")) {
					JOptionPane.showMessageDialog(null, "File with same MD5checksum exists");
				} else {
					JOptionPane.showMessageDialog(null, "Unknown error");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getMD5Checksum(File f) throws Exception {
		InputStream fis = new FileInputStream(f);
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();

		byte[] checksum = complete.digest();
		String result = "";

		for (int i = 0; i < checksum.length; i++) {
			result += Integer.toString((checksum[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	private void refreshUserList() {
		try {
			dos.writeUTF("111");
			ois = new ObjectInputStream(dis);
			@SuppressWarnings("unchecked") // there's no need to check it
			LinkedList<HashMap<String, Object>> userlist = (LinkedList<HashMap<String, Object>>) ois.readObject();
			Iterator<HashMap<String, Object>> useriter = userlist.iterator();
			usermodel.setRowCount(0);// clear the table
			while (useriter.hasNext()) {
				HashMap<String, Object> row = useriter.next();
				Vector<String> rowVector = new Vector<String>();
				rowVector.addElement((String) row.get("Name"));
				rowVector.addElement((String) row.get("IP"));
				usermodel.addRow(rowVector);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void refreshFileList() {
		try {
			dos.writeUTF("112");
			ois = new ObjectInputStream(dis);
			@SuppressWarnings("unchecked") // there's no need to check it
			LinkedList<HashMap<String, Object>> filelist = (LinkedList<HashMap<String, Object>>) ois.readObject();
			Iterator<HashMap<String, Object>> fileiter = filelist.iterator();
			filemodel.setRowCount(0);// clear the table
			while (fileiter.hasNext()) {
				HashMap<String, Object> row = fileiter.next();
				Vector<Object> rowVector = new Vector<Object>();
				rowVector.addElement((String) row.get("fileid"));
				rowVector.addElement((String) row.get("filename"));
				rowVector.addElement(((long) row.get("size")) / 1024 + " kb");
				rowVector.addElement((String) row.get("fileowner"));
				filemodel.addRow(rowVector);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void logout() {
		try {
			dos.writeUTF("102");
			Login login = new Login();
			login.setVisible(true);
			login.setLocationRelativeTo(null);
			socket.close();
			dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void restartMain() {
		try {
			String addr = socket.getInetAddress().getHostAddress();
			Socket tSocket = new Socket(addr, 15810);
			dis.readUTF();
			socket = tSocket;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeFile(String md5, String o) {
		try {
			if (owner.equals(o)) {
				dos.writeUTF("121 " + md5);
			} else {
				JOptionPane.showMessageDialog(null, "You can only remove your files");
				return;
			}
			if (dis.readUTF().equals("022")) {
				JOptionPane.showMessageDialog(null, "File removed");
				refreshFileList();
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeThisAccount() {
		try {
			dos.writeUTF("103 " + owner);
			dos.flush();
			if (dis.readUTF().equals("000")) {
				JOptionPane.showMessageDialog(null, "Account removed");
			}
			Login login = new Login();
			login.setVisible(true);
			login.setLocationRelativeTo(null);
			socket.close();
			dispose();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class service implements Runnable {
		private Socket asocket;
		private ServerSocket sendingSocket;

		public service() {
			try {
				sendingSocket = new ServerSocket(15811);
			} catch (IOException e) {
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					asocket = sendingSocket.accept();
					if (asocket.isConnected()) {
						sending();
					}
				} catch (IOException e) {
				}
			}
		}

		private void sending() {
			try {
				DataInputStream requestSteam = new DataInputStream(new BufferedInputStream(asocket.getInputStream()));
				DataOutputStream messageStream = new DataOutputStream(
						new BufferedOutputStream(asocket.getOutputStream()));
				System.out.println(requestSteam == null);
				String requestInOneLine = requestSteam.readUTF();
				System.out.println(requestInOneLine);
				String[] requests = requestInOneLine.split(" ");
				// requests[0] command code
				// requests[1] file location
				// requests[2] file name
				// requests[3] md5checksum
				if (requests[0].equals("102")) {
					String path = requests[1];
					File file = new File(path);
					if (getMD5Checksum(file).equals(requests[3])) {
						DataInputStream filein = new DataInputStream(
								new BufferedInputStream(new FileInputStream(file)));
						byte[] packet = new byte[1024];
						int length = 0;
						messageStream.writeUTF("001");
						while ((length = filein.read(packet, 0, 1024)) > 0) {
							messageStream.write(packet, 0, length);
							messageStream.flush();
						}
						if (filein != null) {
							filein.close();
						}
					} else {
						messageStream.writeUTF("000");
					}
				} else {
					messageStream.writeUTF("002");
				}
				if (requestSteam != null) {
					requestSteam.close();
				}
				if (messageStream != null) {
					messageStream.close();
				}
				if (asocket != null) {
					asocket.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private class fetch implements Runnable {
		private String addr, filename, fileloc, md5, destination;
		private long size;

		public fetch(String r, String n, String l, String m, long s, String d) {
			addr = r;
			filename = n;
			fileloc = l;
			md5 = m;
			size = s;
			destination = d;
		}

		@Override
		public void run() {
			fetchFile(addr, filename, fileloc, md5, size, destination);
		}

		private void fetchFile(String addr, String filename, String fileloc, String md5, long size,
				String destination) {
			Socket bsocket = null;

			try {
				bsocket = new Socket(addr, 15811);
				DataInputStream adis = new DataInputStream(new BufferedInputStream(bsocket.getInputStream()));
				DataOutputStream ados = new DataOutputStream(new BufferedOutputStream(bsocket.getOutputStream()));
				ados.writeUTF("102 " + fileloc + " " + filename + " " + md5);
				ados.flush();
				String response = adis.readUTF();
				if (response.equals("001")) {
					FileOutputStream fos = new FileOutputStream(destination + "\\" + filename);
					long totalLength = 0;
					int length = 0;
					byte[] packet = new byte[1024];
					while ((length = adis.read(packet, 0, 1024)) > 0) {
						totalLength += length;
						fos.write(packet);
						fos.flush();
						progressBar.setValue((int) (totalLength * 100 / size));
						progressBar.setString(progressBar.getValue() + "%");
					}
					progressBar.setValue(100);
					progressBar.setString(progressBar.getValue() + "%");
					JOptionPane.showMessageDialog(null, "File received");
					fos.flush();
					fos.close();
				} else {
					JOptionPane.showMessageDialog(null, "Download failed");
				}
				adis.close();
				bsocket.close();
				ados.close();
				restartMain();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

package client;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;

public class Login extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2165066008684914077L;
	private JPanel contentPane;
	private JTextField username;
	private JTextField password;
	private JTextField serverAddr;
	private Socket socket;

	public Login() {
		setResizable(false);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 237, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		username = new JTextField();
		username.setEnabled(false);
		username.setBounds(74, 76, 147, 21);
		contentPane.add(username);
		username.setColumns(10);

		JLabel lblNewLabel = new JLabel("Username:");
		lblNewLabel.setBounds(10, 79, 54, 15);
		contentPane.add(lblNewLabel);

		password = new JPasswordField();
		password.setEnabled(false);
		password.setColumns(10);
		password.setBounds(74, 107, 147, 21);
		contentPane.add(password);

		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(10, 110, 54, 15);
		contentPane.add(lblPassword);

		JButton btnLogin = new JButton("Login");
		btnLogin.setEnabled(false);
		btnLogin.setBounds(10, 138, 93, 23);
		contentPane.add(btnLogin);

		JButton btnRegister = new JButton("Register");
		btnRegister.setEnabled(false);
		btnRegister.setBounds(128, 138, 93, 23);
		contentPane.add(btnRegister);

		JLabel lblServerAddress = new JLabel("Server address: ");
		lblServerAddress.setBounds(10, 14, 101, 15);
		contentPane.add(lblServerAddress);

		serverAddr = new JTextField();
		serverAddr.setText("localhost");
		serverAddr.setColumns(10);
		serverAddr.setBounds(121, 11, 100, 21);
		contentPane.add(serverAddr);

		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(10, 42, 211, 23);
		contentPane.add(btnConnect);

		serverAddr.requestFocus();

		MouseListener loginListener = new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {

				if (e.getComponent().equals(btnLogin)) {

					if (username.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Username can't be empty");
						return;
					}

					if (password.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Password can't be empty");
						return;
					}

					try {
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						dos.writeUTF("101 " + username.getText() + " " + password.getText());
						DataInputStream dis = new DataInputStream(socket.getInputStream());
						String reString = dis.readUTF();
						if (reString.equals("020")) {
							setVisible(false);
							MainWindow main = new MainWindow(username.getText(), socket, dis, dos);
							main.setVisible(true);
							dispose();
							return;
						} else if (reString.equals("010")) {
							JOptionPane.showMessageDialog(null, "Username doesn't exist");
							username.requestFocus();
							return;
						} else if (reString.equals("011")) {
							JOptionPane.showMessageDialog(null, "Wrong password");
							password.requestFocus();
							return;
						} else if (reString.equals("404")) {
							JOptionPane.showMessageDialog(null, "Unknown error");
							return;
						}
						dis.close();
						dos.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						return;
					}
				}
			}
		};
		btnLogin.addMouseListener(loginListener);

		MouseListener regListener = new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (username.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Username can't be empty");
					return;
				}
				if (password.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Password can't be empty");
					return;
				}
				if (socket.isConnected()) {
					try {
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						dos.writeUTF("100 " + username.getText() + " " + password.getText());
						DataInputStream dis = new DataInputStream(socket.getInputStream());
						String reString = dis.readUTF();
						System.out.println(reString);
						switch (reString) {
						case "001":
							JOptionPane.showMessageDialog(null, "Username already exists");
							username.requestFocus();
							return;
						case "003":
							JOptionPane.showMessageDialog(null, "User created");
							setVisible(false);
							MainWindow main = new MainWindow(username.getText(), socket, dis, dos);
							main.setVisible(true);
							return;
						case "404":
							JOptionPane.showMessageDialog(null, "Unknown error");
							return;
						default:
							break;
						}
						dis.close();
						dos.close();
					} catch (IOException e2) {

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
		btnRegister.addMouseListener(regListener);

		MouseListener connListener = new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				try {
					socket = new Socket(serverAddr.getText(), 15810);
					if (socket.isConnected()) {
						serverAddr.setEnabled(false);
						btnConnect.setEnabled(false);
						btnLogin.setEnabled(true);
						btnRegister.setEnabled(true);
						username.setEnabled(true);
						password.setEnabled(true);
					}
				} catch (UnknownHostException e1) {
					JOptionPane.showMessageDialog(null, "Can't find the host");
					System.err.println(e1.getMessage());
					e1.printStackTrace();
				} catch (ConnectException e2) {
					JOptionPane.showMessageDialog(null, "Connection refused");
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
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
		btnConnect.addMouseListener(connListener);

	}

}

package jmemorize.gui.swing.dialogs;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;

import jmemorize.core.Main;

public class DropboxPushDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String PREFS_DROPBOX_TOKEN = "dropbox.token";
	final String APP_KEY;
	final String APP_SECRET;

	private JButton btnPull;
	private JButton btnPush;
	private JButton btnLogin;
	private JButton btnUser;
	private DbxAppInfo appInfo;
	private DbxRequestConfig config;
	protected String accessToken;
	private DbxClient client;

	public DropboxPushDialog(JFrame parent) {
		super(parent, true);
		// TODO I18N
		setTitle("Sync With Dropbox");
		initComponents();
		pack();
		List<String> lines = null;
		try {
			File f = new File(DropboxPushDialog.class.getResource("/resource/text/dropbox.txt").toURI());
			lines = Files.readAllLines(f.toPath(), Charset.defaultCharset());
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		if (lines.size() == 2) {
			APP_KEY = lines.get(0).trim();
			APP_SECRET = lines.get(1).trim();
		} else {
			APP_KEY = "";
			APP_SECRET = "";
		}
		appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		config = new DbxRequestConfig("jMemmorizePlus1/1.0", Locale.getDefault().toString());
	}

	private void initComponents() {
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(getLastUser());
		getContentPane().add(getNewUser());
		getContentPane().add(getPull());
		getContentPane().add(getPush());

	}

	private void lock(boolean f) {
		getLastUser().setEnabled(f);
		getNewUser().setEnabled(f);
		getPull().setEnabled(f);
		getPush().setEnabled(f);
	}

	private JButton getPull() {
		if (btnPull == null) {
			btnPull = new JButton("Pull");
			btnPull.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (client == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Use recent token or do a login first.");
						return;
					}

					FileOutputStream outputStream = null;
					try {
						lock(false);
						DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
						System.out.println("Files in the root path:");
						for (DbxEntry child : listing.children) {
							System.out.println("	" + child.name + ": " + child.toString());
						}
						String filename = JOptionPane.showInputDialog(DropboxPushDialog.this, "Enter File Name:");
						File home = new File(System.getProperty("user.home") + "/jMemorize+1");
						if (!home.exists())
							home.mkdir();
						File temp = new File(home, filename);
						outputStream = new FileOutputStream(temp);
						DbxEntry.File downloadedFile = client.getFile("/" + filename, null, outputStream);
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Metadata: " + downloadedFile.toString());
						outputStream.flush();
						outputStream.close();
						Main.getInstance().getFrame().loadLesson(temp);
					} catch (DbxException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} finally {
						lock(true);
					}
				}
			});
		}
		return btnPull;
	}

	private JButton getPush() {
		if (btnPush == null) {
			btnPush = new JButton("Push");
			btnPush.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (client == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Use recent token or do a login first.");
						return;
					}
					if (Main.getInstance().getLesson() == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "No Lesson is loaded.");
						return;
					}

					File inputFile = Main.getInstance().getLesson().getFile();
					FileInputStream inputStream = null;
					try {
						lock(false);
						inputStream = new FileInputStream(inputFile);
						DbxEntry.File uploadedFile = client.uploadFile("/" + inputFile.getName(),
								DbxWriteMode.update(System.currentTimeMillis() + ""), inputFile.length(), inputStream);
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Uploaded: " + uploadedFile.toString());

					} catch (DbxException | IOException e1) {
						e1.printStackTrace();
					} finally {
						try {
							if (inputStream != null)
								inputStream.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						lock(true);
					}

				}
			});
		}
		return btnPush;
	}

	private JButton getNewUser() {
		if (btnLogin == null) {
			btnLogin = new JButton("New Login");
			btnLogin.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
					String authorizeUrl = webAuth.start();
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(authorizeUrl));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					String code = JOptionPane.showInputDialog("Please enter the authorization code: ");
					if (code != null && !code.isEmpty()) {
						try {
							DbxAuthFinish authFinish = webAuth.finish(code);
							accessToken = authFinish.accessToken;
							Main.USER_PREFS.put(PREFS_DROPBOX_TOKEN, accessToken);
							getInfo();
						} catch (DbxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			});
		}
		return btnLogin;
	}

	private JButton getLastUser() {
		if (btnUser == null) {
			btnUser = new JButton("Last Saved Cookie");
			btnUser.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String token = Main.USER_PREFS.get(PREFS_DROPBOX_TOKEN, "");
					if (token == "") {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "No Token is found.");
						return;
					}
					accessToken = token;
					getInfo();
				}
			});
		}
		return btnUser;
	}

	protected void getInfo() {
		client = new DbxClient(config, accessToken);
		try {
			JOptionPane.showMessageDialog(this, "Linked account: " + client.getAccountInfo().displayName);
		} catch (HeadlessException | DbxException e) {
			JOptionPane.showMessageDialog(DropboxPushDialog.this,
					"<HTML>Access Failed. Maybe token is not valid.<br/>Try again login.</HTML>");
			e.printStackTrace();
			return;
		}
	}
}

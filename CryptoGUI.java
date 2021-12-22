// imports
import java.awt.*;
import java.awt.event.*;
import java.awt.GridBagConstraints;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;

// Create a GUI to encrypt and decrypt files
public class CryptoGUI extends JPanel {
	private JPanel keyPanel, keyPanel2, inPanel, buttonPanel, encryptPanel, decryptPanel;
	private JLabel keyLabel, inLabel, statusLabel;
	private JTextArea keyArea, inArea;
	private JScrollPane keyPane, inPane;
	private JButton inButton, encryptButton, decryptButton;
	private File inFile, outFile;
	private FileWriter fw;
	private BufferedWriter bw;
	
	// create GridBagConstraints
	private GridBagConstraints createGBC(int x, int y) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = (x == 0) ? GridBagConstraints.WEST : GridBagConstraints.EAST;
		return c;
	}

	// create CryptoGUI
	public CryptoGUI() {
		// init files
		this.inFile = null;
		this.outFile = null;

		// init panels
		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		this.keyPanel = new JPanel(new GridBagLayout());
		this.keyPanel2 = new JPanel(new BorderLayout());
		this.inPanel = new JPanel(new GridBagLayout());
		this.buttonPanel = new JPanel(new GridLayout(1, 2));
		this.encryptPanel = new JPanel(new FlowLayout());
		this.decryptPanel = new JPanel(new FlowLayout());

		// init labels
		this.keyLabel = new JLabel("Key:");
		this.inLabel = new JLabel("Input File:");
		this.statusLabel = new JLabel("Status: Waiting", JLabel.CENTER);
		
		// init textareas
		this.keyArea = new JTextArea(1, 22);
		this.inArea = new JTextArea(1, 15);

		this.inArea.setEditable(false);

		// init scrollpanes
		this.keyPane = new JScrollPane(this.keyArea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.inPane = new JScrollPane(this.inArea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// init buttons
		this.inButton = new JButton("Select");
		this.encryptButton = new JButton("Encrypt");
		this.decryptButton = new JButton("Decrypt");

		this.inButton.addActionListener(new InListener());
		this.encryptButton.addActionListener(new EncryptListener());
		this.decryptButton.addActionListener(new DecryptListener());

		// assemble
		GridBagConstraints c;

		c = createGBC(0, 0);
		this.keyPanel.add(this.keyLabel, c);
		
		c = createGBC(0, 1);
		this.keyPanel.add(this.keyPane, c);

		this.keyPanel2.add(this.keyPanel, BorderLayout.WEST);
		
		c = createGBC(0, 0);
		add(keyPanel2, c);

		c = createGBC(0, 0);
		this.inPanel.add(this.inLabel, c);
		
		c = createGBC(0, 1);
		this.inPanel.add(this.inPane, c);

		c = createGBC(1, 1);
		this.inPanel.add(this.inButton, c);
		
		c = createGBC(0, 1);
		add(this.inPanel, c);

		this.encryptPanel.add(this.encryptButton);
		this.decryptPanel.add(this.decryptButton);
		this.buttonPanel.add(this.encryptPanel);
		this.buttonPanel.add(this.decryptPanel);
		
		c = createGBC(0, 3);
		add(this.buttonPanel, c);

		c = createGBC(0, 4);
		add(this.statusLabel, c);

		// log
		log("Initialize Session");
	}

	private static void log(String s) {
		try {
			File logFile = new File("log.txt");
			logFile.createNewFile();
			FileWriter fw = new FileWriter(logFile, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("[" + LocalDateTime.now() + "] " + s + System.getProperty("line.separator"));
			bw.close();
		} catch(IOException ioe) {}
	}

	// get file name without extension
	private static String getNameNoExtension(File f) {
		String s = f.getAbsolutePath();
		int sep = s.lastIndexOf(File.separator) + 1;
		String ss = s.substring(sep);
		int dot = ss.indexOf(".");
		return ss.substring(0, dot);
	}

	// get file extension
	private static String getExtension(File f) {
		String s = f.getAbsolutePath();
		int dot = s.lastIndexOf(".") + 1;
		return s.substring(dot);
	}

	// get file path
	private static String getPath(File f) {
		String s = f.getAbsolutePath();
		int sep = s.lastIndexOf(File.separator);
		return s.substring(0, sep) + File.separator;
	}	

	// sets error messages
	private static String getError(Exception e) {
		if(e.getClass().getSimpleName().equals("CustomException")) {
			String s = e.getMessage();
			switch(s) {
				case "BadPaddingException":
					return "Incorrect Key";
				case "IllegalBlockSizeException":
					return "File Not Encrypted";
				default:
					return s;
			}
		} else {
			String s = e.getClass().getSimpleName();
			switch(s) {
				case "NullPointerException":
					return "No Files Selected";
				default:
					return s;
			}
		}
	}
	
	// listener for the input file button
	private class InListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// init JFileChooser
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int val = fc.showOpenDialog(CryptoGUI.this);
			if(val == JFileChooser.APPROVE_OPTION) {
				CryptoGUI.this.inFile = fc.getSelectedFile();
				CryptoGUI.this.inArea.setText(CryptoGUI.this.inFile.getAbsolutePath());
			}
		}
	}

	
	// listener for the encrypt button
	private class EncryptListener implements ActionListener {
		private int count;

		public void actionPerformed(ActionEvent e) {
			try {
				// file
				if(CryptoGUI.this.inFile.isFile()) {
					// check file
					if(!CryptoGUI.this.inFile.getName().contains("enc")) {
						// get output file
						CryptoGUI.this.outFile = new File(CryptoGUI.getPath(CryptoGUI.this.inFile) + CryptoGUI.getNameNoExtension(CryptoGUI.this.inFile) + ".enc." + CryptoGUI.getExtension(CryptoGUI.this.inFile));

						// encrypt file
						MyCryptoUtils.encrypt(CryptoGUI.this.keyArea.getText(), CryptoGUI.this.inFile, CryptoGUI.this.outFile);

						// status report
						CryptoGUI.this.statusLabel.setText("Status: Encrypting...");
						Timer timer = new Timer(1000, new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								CryptoGUI.this.statusLabel.setText("Status: 1 File Encrypted");
							}});
						timer.setRepeats(false);
						timer.start();
						
						// log
						CryptoGUI.log("Encrypt: " + CryptoGUI.this.inFile.getName());

						// delete
						CryptoGUI.this.inFile.delete();

						// reset path
						CryptoGUI.this.inFile = CryptoGUI.this.outFile;
						CryptoGUI.this.inArea.setText(CryptoGUI.this.inFile.getAbsolutePath());
					} else {
						CryptoGUI.this.statusLabel.setText("Status: File Already Encrypted");
					}
					   
				// directory
				} else if(CryptoGUI.this.inFile.isDirectory()) {
					File[] fileList = CryptoGUI.this.inFile.listFiles();
					this.count = 0;
					for(int i = 0;  i < fileList.length; i++) {
						if(!fileList[i].getName().contains("enc")) {
							// bump count
							this.count++;
							
							// get output file
							CryptoGUI.this.outFile = new File(CryptoGUI.getPath(fileList[i]) + CryptoGUI.getNameNoExtension(fileList[i]) + ".enc." + CryptoGUI.getExtension(fileList[i]));

							// encrypt file
							MyCryptoUtils.encrypt(CryptoGUI.this.keyArea.getText(), fileList[i], CryptoGUI.this.outFile);

							// status report
							CryptoGUI.this.statusLabel.setText("Status: Encrypting...");

							// log
							CryptoGUI.log("Encrypt: " + fileList[i].getName());

							// delete
							fileList[i].delete();
						}
					}
					// status report
					Timer timer = new Timer(1000, new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							CryptoGUI.this.statusLabel.setText("Status: " + EncryptListener.this.count + " File(s) Encrypted");
					}});
					timer.setRepeats(false);
					timer.start();
				}
			} catch (Exception ex) {
				// status report
				String errorMessage = CryptoGUI.getError(ex);
				CryptoGUI.this.statusLabel.setText("Status: " + errorMessage);
				
				// log
				CryptoGUI.log("Error: " + errorMessage);

			}
		}
	}

	// listener for the decrypt button
	private class DecryptListener implements ActionListener {
		private int count;

		public void actionPerformed(ActionEvent e) {
			try {
				// file
				if(CryptoGUI.this.inFile.isFile()) {
					// check file
					if(CryptoGUI.this.inFile.getName().contains("enc")) {
						// get output file
						CryptoGUI.this.outFile = new File(CryptoGUI.getPath(CryptoGUI.this.inFile) + CryptoGUI.getNameNoExtension(CryptoGUI.this.inFile) + "." + CryptoGUI.getExtension(CryptoGUI.this.inFile));

						// decrypt file
						MyCryptoUtils.decrypt(CryptoGUI.this.keyArea.getText(), CryptoGUI.this.inFile, CryptoGUI.this.outFile);

						// status report
						CryptoGUI.this.statusLabel.setText("Status: Decrypting...");
						Timer timer = new Timer(1000, new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								CryptoGUI.this.statusLabel.setText("Status: 1 File Decrypted");
							}});
						timer.setRepeats(false);
						timer.start();

						// log
                        CryptoGUI.log("Decrypt: " + CryptoGUI.this.inFile.getName());

						// delete
						CryptoGUI.this.inFile.delete();

						// reset path
						CryptoGUI.this.inFile = CryptoGUI.this.outFile;
						CryptoGUI.this.inArea.setText(CryptoGUI.this.inFile.getAbsolutePath());
					} else {
						CryptoGUI.this.statusLabel.setText("Status: File Not Encrypted");
					}
					   
				// directory
				} else if(CryptoGUI.this.inFile.isDirectory()) {
					File[] fileList = CryptoGUI.this.inFile.listFiles();
					this.count = 0;
					for(int i = 0;  i < fileList.length; i++) {
						if(fileList[i].getName().contains("enc")) {
							// bump count
							this.count++;
							
							// get output file
							CryptoGUI.this.outFile = new File(CryptoGUI.getPath(fileList[i]) + CryptoGUI.getNameNoExtension(fileList[i]) + "." + CryptoGUI.getExtension(fileList[i]));

							// decrypt file
							MyCryptoUtils.decrypt(CryptoGUI.this.keyArea.getText(), fileList[i], CryptoGUI.this.outFile);

							// status report
							CryptoGUI.this.statusLabel.setText("Status: Decrypting...");
					
							// log
                            CryptoGUI.log("Decrypt: " + fileList[i].getName());

							// delete
							fileList[i].delete();
						}
					}
					// status report
					Timer timer = new Timer(1000, new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							CryptoGUI.this.statusLabel.setText("Status: " + DecryptListener.this.count + " File(s) Decrypted");
					}});
					timer.setRepeats(false);
					timer.start();
				}
			} catch (Exception ex) {
				// status report
				String errorMessage = CryptoGUI.getError(ex);
				CryptoGUI.this.statusLabel.setText("Status: " + errorMessage);

				// log
				CryptoGUI.log("Error: " + errorMessage);
			}
		}
	}

	public static void onExit() {
		CryptoGUI.log("Close Session");
		System.exit(0);
	}

	// run		
	public static void main(String[] args) {
		// create gui
		JFrame frame = new JFrame("CryptoGUI");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onExit();
		}});
		frame.setResizable(false);
		frame.add(new CryptoGUI());
		frame.pack();
		frame.setVisible(true);
	}
}
		

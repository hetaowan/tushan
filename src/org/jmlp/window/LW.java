package org.jmlp.window;

// ToolbarFrame2.java
// The Swing-ified button example 
//
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.swing.*;

public class LW extends Frame {
	// This time, let's use JButtons!

	Button[] buts;
	Button[] rightbuts;
	Button[] wrongbuts;
	JTextField[] predfields;
	JPanel[] linePans;
	String[] hostlists;
	JScrollPane scroll;
	Hashtable keyWordsHash;
	Hashtable bidHash;
	JTextArea ta;
	JPanel hostPan;
	JPanel rightPan;
	JPanel wrongPan;
	JPanel wholePan;
	JPanel predPan;

	JPanel winPan;
	JPanel bottomPan;
	Frame wfram;
	int selbuti = 0;
	Button nextbut;
	Button prebut;
	Button exitbut;
	Button clearbut;

	FileWriter rightWriter;
	FileWriter wrongWriter;

	PrintWriter rpwriter;
	PrintWriter wpwriter;

	public LW() {
		super("Toolbar Example (Swing)");
		setSize(1400, 1000);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		ActionListener printListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String lnfName = e.getActionCommand();
				System.out.println(lnfName);

				if (lnfName.equals("next")) {
					for (int i = selbuti; i < selbuti + 25; i++) {
						// hostPan.remove(buts[i]);
						// rightPan.remove(rightbuts[i]);
						// wrongPan.remove(wrongbuts[i]);
						// predPan.remove(predfields[i]);
						winPan.remove(linePans[i]);
					}
					// hostPan.remove(nextbut);
					// rightPan.remove(prebut);
					// wrongPan.remove(exitbut);
					// predPan.remove(clearbut);
					bottomPan.remove(nextbut);
					bottomPan.updateUI();
					// hostPan.updateUI();

					selbuti += 25;
					for (int i = selbuti; i < selbuti + 25; i++) {
						// hostPan.add(buts[i]);
						// rightPan.add(rightbuts[i]);
						// wrongPan.add(wrongbuts[i]);
						// predPan.add(predfields[i]);
						winPan.add(linePans[i]);
					}
					bottomPan.add(nextbut);
					bottomPan.updateUI();
					winPan.add(bottomPan);
					// hostPan.add(nextbut);
					// rightPan.add(prebut);
					// wrongPan.add(exitbut);
					// predPan.add(clearbut);
					// hostPan.updateUI();
					// rightPan.updateUI();
					// wrongPan.updateUI();
					// predPan.updateUI();
					winPan.updateUI();
					ta.setText(keyWordsHash.get(hostlists[selbuti]) + "");
					// wfram.repaint();
					// wfram.add(hostPan);
					// wfram.add(rightPan);
					// wfram.add(wrongPan);
				}
				if (!lnfName.equals("next") && !lnfName.equals("pre")
						&& !lnfName.equals("exit")
						&& !Pattern.matches("right\\d+", lnfName)
						&& !Pattern.matches("wrong\\d+", lnfName)) {
					ta.setText(keyWordsHash.get(lnfName.trim()) + "");
					int tempbid = Integer.parseInt(bidHash.get(lnfName.trim())
							+ "");
					buts[tempbid].setForeground(Color.blue);
				}

				if (Pattern.matches("right\\d+", lnfName)) {
					int hid = Integer.parseInt(lnfName.substring(
							lnfName.indexOf("right") + 5, lnfName.length()));
					// System.out.println("hid:"+hid);
					// System.out.println(hostlists[hid]);
					rpwriter.println(hostlists[hid]);
					rpwriter.flush();
					rightbuts[hid].setForeground(Color.green);
				}

				if (Pattern.matches("wrong\\d+", lnfName)) {
					int hid = Integer.parseInt(lnfName.substring(
							lnfName.indexOf("wrong") + 5, lnfName.length()));
					// System.out.println("hid:"+hid);
					// System.out.println(hostlists[hid]);
					wpwriter.println(hostlists[hid]);
					wpwriter.flush();
					wrongbuts[hid].setForeground(Color.red);
				}
			}
		};

		// JPanel works similarly to Panel, so we'll use it.
		hostPan = new JPanel();
		hostPan.setLayout(new BoxLayout(hostPan, BoxLayout.Y_AXIS));
		rightPan = new JPanel();
		rightPan.setLayout(new BoxLayout(rightPan, BoxLayout.Y_AXIS));
		wrongPan = new JPanel();
		wrongPan.setLayout(new BoxLayout(wrongPan, BoxLayout.Y_AXIS));
		predPan = new JPanel();
		predPan.setLayout(new BoxLayout(predPan, BoxLayout.Y_AXIS));
		winPan = new JPanel();
		winPan.setLayout(new BoxLayout(winPan, BoxLayout.Y_AXIS));
		bottomPan = new JPanel();
		bottomPan.setLayout(new BoxLayout(bottomPan, BoxLayout.X_AXIS));
		FileReader fr = null;
		BufferedReader br = null;
		buts = null;
		int hn = 6571;
		buts = new Button[6571];
		rightbuts = new Button[6571];
		wrongbuts = new Button[6571];
		predfields = new JTextField[6571];
		hostlists = new String[6571];
		linePans = new JPanel[6571];

		String cFName = "zaoyin_xiuzheng";
		bidHash = new Hashtable();

		FileReader predfr = null;
		BufferedReader predbr = null;

		try {
			rightWriter = new FileWriter(new File("label/left_sel/" + cFName
					+ ".txt"), true);
			rpwriter = new PrintWriter(rightWriter);
			wrongWriter = new FileWriter(new File("label/left_sel_wrong/"
					+ cFName + ".txt"), true);
			wpwriter = new PrintWriter(wrongWriter);
			predfr = new FileReader(new File("pred_dir/zaoyin_host.txt"));
			predbr = new BufferedReader(predfr);
			String predline = "";
			StringTokenizer predst = null;
			String predhost = "";
			String predcate = "";
			Hashtable predHash = new Hashtable();
			while ((predline = predbr.readLine()) != null) {
				predst = new StringTokenizer(predline, " \t");
				if (predst.countTokens() < 2) {
					continue;
				}

				predhost = predst.nextToken();
				predhost = predhost.trim();
				predcate = predst.nextToken();
				predcate = predcate.trim();
				System.out.println(predhost + " :" + predcate);
				if (!predHash.containsKey(predhost)) {
					predHash.put(predhost, predcate);
				}
			}

			predfr.close();
			predbr.close();
			fr = new FileReader(new File("pred_dir/zaoyin_sam.txt"));
			br = new BufferedReader(fr);
			String line = "";
			String host = "";
			StringTokenizer st = null;
			String[] items = null;
			String keywords = "";
			int index = 0;
			int bi = 0;
			keyWordsHash = new Hashtable();
			while ((line = br.readLine()) != null) {
				// System.out.println("bi="+bi);
				// System.out.println(line);
				st = new StringTokenizer(line.trim(), " \t");
				items = new String[st.countTokens()];
				index = 0;
				keywords = "";
				// System.out.println("st.countTokens:"+st.countTokens());
				if (st.countTokens() < 2) {
					continue;
				}
				while (st.hasMoreTokens()) {
					items[index] = st.nextToken();
					index++;
				}
				for (int k = 1; k < items.length; k++) {
					keywords += items[k];
				}
				host = items[0].trim();
				hostlists[bi] = host;
				if (!bidHash.containsKey(host)) {
					bidHash.put(host, bi);
				}
				if (!keyWordsHash.containsKey(host)) {
					keyWordsHash.put(host, keywords);
				}
				// System.out.println(host);
				for (int i = 1; i < items.length; i++) {
					keywords += items[i];
				}
				// System.out.println("keywords:"+keywords);
				buts[bi] = new Button(host);
				buts[bi].addActionListener(printListener);

				rightbuts[bi] = new Button("right" + bi);
				rightbuts[bi].addActionListener(printListener);

				wrongbuts[bi] = new Button("wrong" + bi);
				wrongbuts[bi].addActionListener(printListener);
				System.out.println(predHash.get(host) + "...");
				predfields[bi] = new JTextField(predHash.get(host) + "");
				predfields[bi].setSize(200, 100);
				predfields[bi].addActionListener(printListener);

				linePans[bi] = new JPanel();
				linePans[bi].setLayout(new BoxLayout(linePans[bi],
						BoxLayout.X_AXIS));
				linePans[bi].add(buts[bi]);
				linePans[bi].add(rightbuts[bi]);
				linePans[bi].add(wrongbuts[bi]);
				linePans[bi].add(predfields[bi]);
				bi++;
			}
		} catch (Exception e) {

		}
		// add(toolbar, BorderLayout.NORTH);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		for (int i = selbuti; i < selbuti + 25; i++) {
			// hostPan.add(buts[i]);
			// rightPan.add(rightbuts[i]);
			// wrongPan.add(wrongbuts[i]);
			// predPan.add(predfields[i]);
			winPan.add(linePans[i]);
		}

		ta = new JTextArea();
		Font font = new Font("宋体", Font.PLAIN, 14);
		ta.setFont(font);
		ta.setSize(800, 800);
		ta.setLineWrap(true);
		// ta.setWrapStyleWord(true);
		// scroll = new JScrollPane(ta);
		// scroll.setSize(800, 800);
		// Add three lines of text to the JTextArea.
		ta.setText(keyWordsHash.get(hostlists[selbuti]) + "");
		nextbut = new Button("next");
		nextbut.addActionListener(printListener);
		prebut = new Button("pre");
		prebut.addActionListener(printListener);
		exitbut = new Button("exit");
		exitbut.addActionListener(printListener);
		clearbut = new Button("clear");
		clearbut.addActionListener(printListener);
		bottomPan.add(nextbut);
		winPan.add(bottomPan);
		// hostPan.add(nextbut);
		// rightPan.add(prebut);
		// wrongPan.add(exitbut);
		// predPan.add(clearbut);

		wfram = this;

		// wfram.add(hostPan);
		// wfram.add(rightPan);
		// wfram.add(wrongPan);
		// wfram.add(predPan);
		wfram.add(winPan);
		wfram.add(ta);
		// Add the L&F controls.
	}

	public static void main(String args[]) {
		LW tf2 = new LW();
		tf2.setVisible(true);
	}
}
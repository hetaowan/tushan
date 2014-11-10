package org.jmlp.window;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jmlp.file.utils.FileToArray;
import org.jmlp.str.basic.SSO;
import org.jmlp.web.WordAnalyticSE;
import org.jmlp.web.WordResult;
import org.jmlp.web.WordResultTitle;

public class WordPick extends Frame {

	JTextField wordField;

	Button nextBut;
	
	Button waBut;

	Button lastBut;

	Button saveBut;

	JLabel status;

	JPanel workPan;

	JPanel statPan;

	JPanel waPan;
	
	NewActionListener listener;
	
	JTextArea wordAnalytic;

	private ArrayList<String> wordList = new ArrayList<String>();

	private HashMap<String, Boolean> wordStat = new HashMap<>();

	private HashMap<String, Boolean> pickedWord = new HashMap<>();

	Frame wfram;

	private int index = 0;

	private PrintWriter pickPW;

	private PrintWriter statPW;

	private PrintWriter todoPW;
	
	private WordAnalyticSE wase;

	public WordPick() {
		super("Toolbar Example (Swing)");
		this.setSize(800, 600);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		wase=new WordAnalyticSE();
		wase.init();
		wase.setWrType(0);
		
		this.setLayout(new GridLayout(3, 1));
		try {
			pickPW = new PrintWriter(new FileWriter(
					"bkw_ten_day_words/pick.txt", true));
			statPW = new PrintWriter(new FileWriter(
					"bkw_ten_day_words/pickstat.txt", true));
			todoPW = new PrintWriter(new FileWriter(
					"bkw_ten_day_words/picktodo.txt", true));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		readWords();
		wfram = this;
		listener = new NewActionListener();
		initWindows();


	}

	private class NewActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String lnfName = e.getActionCommand();
			System.out.println(lnfName);

			if (lnfName.equals("nextword")) {
				wordAnalytic.setText("");
				statPW.println(wordList.get(index) + "\001"
						+ wordStat.get(wordList.get(index)));
				statPW.flush();
				index++;
				wordField.setText(wordList.get(index));
				// status.setText(wordStat.get(wordList.get(index)) + "");
				resetStat();
			} else if (lnfName.equals("pre")) {
				wordAnalytic.setText("");
				statPW.println(wordList.get(index) + "\001"
						+ wordStat.get(wordList.get(index)));
				statPW.flush();
				index--;
				wordField.setText(wordList.get(index));
				statPW.flush();
				resetStat();
			} else if (lnfName.equals("saveword")) {
				wordStat.put(wordList.get(index), true);
				// status.setText(wordStat.get(wordList.get(index)) + "");
				resetStat();
				// status.updateUI();
				// statPan.updateUI();

				pickPW.println(wordList.get(index));
				pickPW.flush();
			}else if(lnfName.equals("analysis")){
				
				WordResult wrt=wase.parse(wordList.get(index));			
				wordAnalytic.setText(wrt.toString());
			}
			

		}
	}

	public void initWindows() {
																																																																																																																																															workPan = new JPanel();
																																																																																																																																															workPan.setSize(600, 50);
		workPan.setLayout(new BoxLayout(workPan, BoxLayout.X_AXIS));

		wordField = new JTextField(wordList.get(index));
		wordField.setSize(100, 50);
		wordField.setLocation(0, 0);
		wordField.addActionListener(listener);

		nextBut = new Button("nextword");
		nextBut.setSize(100, 50);
		nextBut.addActionListener(listener);

		waBut=new Button("analysis");
		waBut.setSize(100, 50);
		waBut.addActionListener(listener);
		
		lastBut = new Button("pre");
		lastBut.setSize(100, 50);
		lastBut.addActionListener(listener);

		saveBut = new Button("saveword");
		saveBut.setSize(100, 50);
		saveBut.addActionListener(listener);

		workPan.add(wordField);
		workPan.add(nextBut);
		workPan.add(saveBut);
		workPan.add(waBut);	
		workPan.add(lastBut);

		statPan = new JPanel();
		statPan.setSize(800, 100);
		statPan.setLayout(new BoxLayout(statPan, BoxLayout.X_AXIS));

		status = new JLabel("test");
		resetStat();
		status.setSize(200, 100);
		statPan.add(status);

		waPan=new JPanel();
		waPan.setSize(800, 300);
		waPan.setLayout(new BoxLayout(waPan, BoxLayout.X_AXIS));
		
		
		wordAnalytic = new JTextArea();
		Font font = new Font("宋体", Font.PLAIN, 14);
		wordAnalytic.setFont(font);
		wordAnalytic.setSize(800, 300);
		wordAnalytic.setLineWrap(true);
		waPan.add(wordAnalytic);
		
		wfram.add(workPan);
		wfram.add(wordAnalytic);
		wfram.add(statPan);

	}

	public void resetStat() {
		status.setText("stat:" + wordStat.get(wordList.get(index))
				+ "         index:" + index + "          tot:"
				+ wordList.size());
	}

	public void readWords() {
		try {
			ArrayList<String> oldList = FileToArray
					.fileToArrayList("bkw_ten_day_words/bat1_no.txt");
			ArrayList<String> pickedList = FileToArray
					.fileToArrayList("bkw_ten_day_words/pickstat.txt");

			String word = "";
			String[] tokens = null;
			String pair = "";
			for (int i = 0; i < pickedList.size(); i++) {
				pair = pickedList.get(i);
				if (SSO.tioe(pair)) {
					continue;
				}
				pair = pair.trim();
				tokens = pair.split("\001");
				word = tokens[0];
				if (SSO.tioe(word)) {
					continue;
				}
				word = word.trim();
				pickedWord.put(word, true);
			}

			pickedList = null;

			for (int i = 0; i < oldList.size(); i++) {
				word = oldList.get(i);
				if (SSO.tioe(word)) {
					continue;
				}
				word = word.trim();

				if (!(pickedWord.containsKey(word))) {
					wordList.add(word);
					todoPW.println(word);
				}

			}
			todoPW.close();
			pickedWord = null;
			oldList = null;

			for (int i = 0; i < wordList.size(); i++) {
				wordStat.put(wordList.get(i), false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		WordPick wp = new WordPick();
		wp.setVisible(true);
	}

}

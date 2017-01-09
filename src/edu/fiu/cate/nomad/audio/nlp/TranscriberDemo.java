package edu.fiu.cate.nomad.audio.nlp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.cmu.sphinx.result.Lattice;
import edu.fiu.cate.nomad.audio.PSEyeAudio;

public class TranscriberDemo {     
	
	public TranscriberDemo(){
		
		TextOutputGUI gui = new TextOutputGUI();

		Configuration configuration = new Configuration();

		configuration
				.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration
				.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration
				.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		
		System.out.println("Configuration Loaded.....");

		try {
			PSEyeAudio[] cams = PSEyeAudio.getAvailablePSEye();
			LiveSpeechRecognizer recognizer;
			if(cams.length>0)
				recognizer = new LiveSpeechRecognizer(configuration, cams[0].getMixer().getMixerInfo());
			else 
				recognizer = new LiveSpeechRecognizer(configuration);

			System.out.println("Recognizer Created.....");

			SpeechResult result;
			
//			// Start recognition process pruning previously cached data.
//			Stats stats = recognizer.createStats(1);
//			System.out.println("Training started");
//			recognizer.startRecognition(true);
//			result = recognizer.getResult();
//			while ((result = recognizer.getResult()) != null) {
//				stats.collect(result);
//				System.out.format("Hypothesis: %s\n", result.getHypothesis());
//				gui.systemOutput.append("Hypothesis: " + result.getHypothesis()+"\n");
//				if (result.getHypothesis().trim().equals("training over"))
//					break;
//			}
//			// Pause recognition process. It can be resumed then with
//			// startRecognition(false).
//			System.out.println("Trainning stoped");
//			recognizer.stopRecognition();
//
//			Transform transform = stats.createTransform();
//			recognizer.setTransform(transform);
			
			System.out.println("Started Listenning");
			recognizer.startRecognition(true);
			result = recognizer.getResult();
			while ((result = recognizer.getResult()) != null) {
				System.out.format("Hypothesis: %s\n", result.getHypothesis());
				gui.systemOutput.append("Hypothesis: " + result.getHypothesis()+"\n");
				String[] hyps = result.getNbest(3).toArray(new String[3]);
				for(String h: hyps)
					gui.systemOutput.append("\t"+h+"\n");
				gui.systemOutput.append(result.getResult().getBestPronunciationResult()+"\n");
				Lattice lattice = result.getLattice();
				List<String> latticePaths = lattice.allPaths();
				for(String h: latticePaths)
					gui.systemOutput.append("\t"+h+"\n");
				if (result.getHypothesis().trim().equals("exit"))
					break;
			}
			// Pause recognition process. It can be resumed then with
			// startRecognition(false).
			System.out.println("Stoped Listenning");
			recognizer.stopRecognition();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public class TextOutputGUI extends JFrame {
		
		private static final long serialVersionUID = -4024381676778839713L;

		public JTextArea 	systemOutput = new JTextArea();
		public JTextArea 	textToTalk = new JTextArea();		
		public JButton 		talkButton = new JButton("Process");
		
		public TextOutputGUI(){
			
			setTitle("Transcriber Demo.");
			setBounds(0, 0, 500, 500);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			
			GroupLayout groupLayout = new GroupLayout(getContentPane());
			groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 473, GroupLayout.PREFERRED_SIZE)
							.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
								.addComponent(textToTalk, GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
								.addGap(18)
								.addComponent(talkButton)))
						.addContainerGap())
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
					.addGroup(groupLayout.createSequentialGroup()
						.addGap(26)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 371, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addGroup(groupLayout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
								.addComponent(talkButton)
								.addGap(35))
							.addGroup(groupLayout.createSequentialGroup()
								.addGap(31)
								.addComponent(textToTalk, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
								.addContainerGap())))
			);
			
			systemOutput.setEditable(false);
			scrollPane.setViewportView(systemOutput);
			getContentPane().setLayout(groupLayout);
			
			talkButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String txt = textToTalk.getText();
					textToTalk.setText("");
					
					systemOutput.append(txt+"\n");
					textToTalk.requestFocus();
//					systemOutput.append(processText(txt));
					
				}
			});
			
			textToTalk.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						e.consume();
					    talkButton.doClick();
					};	
				}
			});
			
			setVisible(true);
		}		
	}
	
}

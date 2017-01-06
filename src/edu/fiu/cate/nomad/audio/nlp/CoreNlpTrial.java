package edu.fiu.cate.nomad.audio.nlp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefGraphAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntTuple;
import edu.stanford.nlp.util.Pair;

public class CoreNlpTrial {
	
	StanfordCoreNLP pipeline;
	
	public CoreNlpTrial(){
	    
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);

		new TextInputGUI();
	}
	
	public String processText(String txt){

		String outputText = "";

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(txt);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

//		outputText+="Document Annotation Types:\n";
//		for (Class<?> c : document.keySet()) {
//			outputText+="\t" + c.getSimpleName()+"\n";
//		}
//		outputText+="Sentence Annotation Types:"+"\n";
//		for (Class<?> c : sentences.get(0).keySet()) {
//			outputText+="\t" + c.getSimpleName()+"\n";
//		}

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
			for (CoreLabel token : tokens) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);

				outputText+=word + ": " + pos + ", " + ne+"\n";
				outputText+="\t" + PennPartOfSpeech.getDescription(pos)+"\n";
			}

			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeAnnotation.class);
			outputText += tree.toString();
			outputText += "\n\n";

			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			outputText += dependencies.toString();

			// understand(mind, tokens, dependencies);
		}

		outputText += "\n\n";

		// This is the coreference link graph
		// Each chain stores a set of mentions that link to each other,
		// along with a method for getting the most representative mention
		// Both sentence and token offsets start at 1!
		// Map<Integer, CorefChain> graph =
		// document.get(CorefChainAnnotation.class);

		// this is the coreference link graph
		// each link stores an arc in the graph; the first element in the Pair
		// is the source, the second is the target
		// each node is stored as <sentence id, token id>. Both offsets start at
		// 1!
		List<Pair<IntTuple, IntTuple>> graph = document.get(CorefGraphAnnotation.class);
		if (graph != null)
			outputText += graph.toString();

		return outputText;
	}
	
	public class TextInputGUI extends JFrame {
		
		private static final long serialVersionUID = -4024381676778839713L;

		JTextArea 	systemOutput = new JTextArea();
		JTextArea 	textToTalk = new JTextArea();		
		JButton 	talkButton = new JButton("Process");
		
		public TextInputGUI(){
			
			setTitle("CoreNLP Text Input.");
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
					systemOutput.append(processText(txt));
					
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

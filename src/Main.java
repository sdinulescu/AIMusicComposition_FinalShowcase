/* Stejara Dinulescu
 * Main class to run program
 */

import processing.core.*;
import oscP5.*;
import netP5.*;

import java.io.*; 
import java.util.*;


public class Main extends PApplet {
	public static String abracadabra = "abracadabra"; //creates test string abracadabra
	public static String abcccdaadcdaabcad = "abcccdaadcdaabcad"; //creates test string abcccdaadcdaabcad
	public static String mhll = "edcdeeedddeggedcdeeeeddedc"; //creates test string mary had a little lamb
	public static int L = 3; //L value
	public static double pMin = 0.07;
	public static int r = 2;
	public static int countBack = 3;
	public static int times = 2;
	//public static double emptyContextProb = Math.random();
	public static double g = 0.01; //for manually inputting
	public static double gMaxRange; //gMaxRange is 1/N, which is calculated according to the tree
	
	public static int counter = 0;
	
	PFont f;
	
	MelodyPlayer player;
	
	static String strForPST = "";
	static OscP5 object; //creates an OscP5 object
	static NetAddress myRemoteLocation;

	
	static ArrayList<Integer> pitches = new ArrayList<Integer>();
	
	String filePath = "Users/stejaradinulescu/Documents/SMU/Year IV/Spring Semester/CRCP 1192/ShowcaseProject/file.txt";
	File file;
	BufferedReader br; 


	
	public static void main(String[] args) {
		//OSCPort sender = new OSCPort();
		
		PApplet.main("Main");
	}
	
	public void settings() {
		size(800, 800);
		

		System.out.println("--------------------------- MIDI ---------------------------");
//		try {
//			midi = new MidiToNotes();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	public void setup() {
		//reading in the file with the filePath application
		String str = ""; 
		file = new File("/Users/stejaradinulescu/Documents/SMU/Year IV/Spring Semester/CRCP 1192/ShowcaseProject/res/myFile.txt"); 
		try {
			br = new BufferedReader(new FileReader(file));
			str = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("full path: " + str);

		System.out.println("Opening Max application");
		launch(str);
		

		background(255);

		f = createFont("Arial",20,true); // Arial, 16 point, anti-aliasing on
		
		textAlign(CENTER);
		textFont(f);
		fill(0);
		text("Welcome! Use the attached MIDI keyboard to play a series of notes.", width/2, 140);
		text("This AI system will create a mini-composition based on what you play!", width/2, 170);

		//set up melody player
		player = new MelodyPlayer(this, 120); //tempo BPM
		player.setupMidi();
		
		//set up OSC messages
		myRemoteLocation = new NetAddress("127.0.0.1",8888);
		object = new OscP5(this, 8888);


		
//		System.out.println("--------------------------- PST Test ---------------------------");
//		testString(mhll, L, pMin, r);
//		testString(abracadabra, L, pMin, r);	
	}
	
	public void draw() {
		text("Press the red button when you are done recording your musical phrase", width/2, 230);
	
		fill(255, 0, 0);
		rect(width/2 - 50, height/2 - 50, 100, 100);
		
		if (mousePressed) { 
			strForPST = player.getStringToTrain(); //gets the phrase played and then sends it to PST
			System.out.println(strForPST);
			if (strForPST != null) { //send to PST to train
				testString(strForPST, L, pMin, r); //get the trained output string
				handlePitches(); //convert to pitches and then send to max
			}
		}		
	}
	
	public static void testString(String input, int L, double pMin, int r) {
		System.out.println(input);
		Tree<Character> treeStruct = new Tree<Character>(); //creates tree structure for desired list
		treeStruct.search(input, L); //searches through motives to create tree structure
		System.out.println("Tree structure before elimination: ");
		treeStruct.list();
		System.out.println("-------------------------------------------------------");
		treeStruct.eliminateEmpirical(input, pMin); //first step: eliminate empirical probabilities based on pMin
		System.out.println("Tree structure after pMin elimination: ");
		treeStruct.list();
		System.out.println("-------------------------------------------------------");
		System.out.println("Conditional Probability Elimination: ");
		treeStruct.eliminateConditional(input, r);
		System.out.println("Tree structure after r elimination: ");
		treeStruct.list();
		System.out.println("-------------------------------------------------------");
		System.out.println("Probabilities for each next: ");
		treeStruct.calcNextProbs(input);
		treeStruct.printProbs();
		System.out.println("-------------------------------------------------------");
		System.out.println("Smoothing: ");
		gMaxRange = calcNVal(input);
		checkGVal(g);
		treeStruct.implementSmoothing(g);
		treeStruct.printProbs();
		System.out.println("-------------------------------------------------------");
		System.out.println("Generate: ");
		treeStruct.generateString(L);
		System.out.println("GeneratedString: " + treeStruct.getGeneratedString());
		System.out.println("-------------------------------------------------------");
		System.out.println("Check infinite looping problem: ");
		treeStruct.infiniteLoop(countBack, times);
		System.out.println("Final generated string: " + treeStruct.getGeneratedString());
		strForPST = treeStruct.getGeneratedString(); //resets that string to the new melody phrase
		
	}
	
	
	public static void checkGVal(double g) {
		if (g <= 0 || g >= gMaxRange) { //prompt user to pick another g value
			System.out.println("g value is not within the range of 0 to 1 divided by the input string length. Please pick a value greater than 0 and less than " + gMaxRange);
		}
		return;
	}
	
	public static double calcNVal(String input) { //calculates g and N values for smoothing purposes
		int N = 0; 
		N = input.length();
		double Nval = (double)1/N;
		//System.out.println("1/N = " + Nval);
		return Nval;
	}
	
	public static void handlePitches() { 
		//convert generated string to midi
		charToMidi(strForPST); 
		System.out.println(pitches);
		//send pitches to Max
		if (!pitches.isEmpty()) {
			sendOSC();
		}
	}
	
	public static void charToMidi(String str) { //converts note letter to midi note
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == 'a') {
				pitches.add(69);
			} else if (str.charAt(i) == 'b') {
				pitches.add(71);
			} else if (str.charAt(i) == 'c') {
				pitches.add(72);
			} else if (str.charAt(i) == 'd') {
				pitches.add(74);
			} else if (str.charAt(i) == 'e') {
				pitches.add(76);
			} else if (str.charAt(i) == 'f') {
				pitches.add(77);
			} else if (str.charAt(i) == 'g') {
				pitches.add(79);
			}
		}

	}
	
	public static void sendOSC() { //sends OSC to max
		int i = 0;
		while (i != pitches.size() ) {
			OscMessage myMessage = new OscMessage("/pitches");
			myMessage.add(pitches.get(i)); // add each pitch in the melody phrase to the osc message
			object.send(myMessage, myRemoteLocation); //send to max
			System.out.println("sending... " + pitches.get(i));
			i++;
		    try {
				Thread.sleep(1000); //send every 1 second -> fixed rhythm
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
	
	
//Stejara Dinulescu
//Main class to run program

import netP5.*;
import oscP5.*;

OscP5 object; //creates an OscP5 object
NetAddress myRemoteLocation; //specifies address for osc messages

String abracadabra = "abracadabra"; //creates test string abracadabra
String abcccdaadcdaabcad = "abcccdaadcdaabcad"; //creates test string abcccdaadcdaabcad
String mhll = "edcdeeedddeggedcdeeeeddedc"; //creates test string mary had a little lamb
int L = 3; //L value
double pMin = 0.07;
int r = 2;
int countBack = 3;
int times = 2;
//public static double emptyContextProb = Math.random();
double g = 0.01; //for manually inputting
double gMaxRange; //gMaxRange is 1/N, which is calculated according to the tree

String generatedStr = "";
void setup() { 
  object = new OscP5(this, 12001); // creates new OscP5 object, listening port 12001
  myRemoteLocation = new NetAddress("127.0.0.1", 8888); //where it is being received

  //testString(abracadabra, L, pMin, r);
  System.out.println("--------------------------------------------------------------------------------");
  generatedStr = testString(mhll, L, pMin, r);
  
  
  OscMessage m = new OscMessage("/string");
  m.add(generatedStr);
  println("sending: " + m.get(0).stringValue());
  object.send(m, myRemoteLocation);
  println("osc sent");
}


String testString(String input, int L, double pMin, int r) {
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
  return treeStruct.getGeneratedString();
}

void checkGVal(double g) {
  if (g <= 0 || g >= gMaxRange) { //prompt user to pick another g value
    System.out.println("g value is not within the range of 0 to 1 divided by the input string length. Please pick a value greater than 0 and less than " + gMaxRange);
  }
  return;
}

double calcNVal(String input) { //calculates g and N values for smoothing purposes
  int N = 0; 
  N = input.length();
  double Nval = (double)1/N;
  //System.out.println("1/N = " + Nval);
  return Nval;
}

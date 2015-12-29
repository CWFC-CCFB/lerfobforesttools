package lerfob.sampling.tests;

import java.util.ArrayList;
import java.util.List;

public class Population {

	private final List<Tree> trees;
	private final double widthM;
	private final double heightM;
	
	public Population(int nbTrees, double widthM, double heightM) {
		trees = new ArrayList<Tree>();
		this.widthM = widthM;
		this.heightM = heightM;
		double xCoordM;
		double yCoordM;
		for (int i = 0; i < nbTrees; i++) {
			xCoordM = Math.random() * widthM;
			yCoordM = Math.random() * heightM;
			trees.add(new Tree(xCoordM, yCoordM));
		}
	}

	
	public static void main(String[] args) {
		Population pop = new Population(100,100,100);
	}
	
	
}

package lerfob.sampling.tests;

public class Tree extends LocatedPoint {

	private double samplingRadiusM = 10d;
	
	/**
	 * Constructor.
	 * @param xCoordM the x coordinate in m
	 * @param yCoordM the y coordinate in m
	 */
	public Tree(double xCoordM, double yCoordM) {
		super(xCoordM, yCoordM);
	}

	/**
	 * This method returns the sampling area for this tree.
	 * @return the sampling area in m2
	 */
	public double getSamplingAreaM2() {
		return Math.PI * samplingRadiusM * samplingRadiusM;
	}

	/**
	 * This method returns the common sampling area between this tree and the tree in argument.
	 * @param otherTree a Tree instance
	 * @return the common sampling area in m2
	 */
	public double getCommonSamplingAreaM2(Tree otherTree) {
		double distance = getDistanceM(otherTree);
		if (distance > this.samplingRadiusM + otherTree.samplingRadiusM) {	// trees are too far away, there is no common area
			return 0d;
		} else if (otherTree.samplingRadiusM < samplingRadiusM && otherTree.samplingRadiusM + distance <= samplingRadiusM) {	// means the other tree sampling area is entirely located within this sampling area
			return otherTree.getSamplingAreaM2();
		} else if (samplingRadiusM < otherTree.samplingRadiusM && samplingRadiusM + distance <= otherTree.samplingRadiusM) {	// means this sampling area is entirely located within the other tree sampling area
			return getSamplingAreaM2();
		} else {
			double cosThis = (samplingRadiusM * samplingRadiusM - otherTree.samplingRadiusM * otherTree.samplingRadiusM + distance*distance)/(2*distance*samplingRadiusM);
			double angleThis = Math.acos(cosThis);
			double cosThat = (otherTree.samplingRadiusM * otherTree.samplingRadiusM - samplingRadiusM * samplingRadiusM + distance*distance)/(2*distance*otherTree.samplingRadiusM);
			double angleThat = Math.acos(cosThat);
			double thisArea = samplingRadiusM * samplingRadiusM * (angleThis - Math.sin(angleThis)*cosThis);
			double thatArea = otherTree.samplingRadiusM * otherTree.samplingRadiusM * (angleThat - Math.sin(angleThat)*cosThat);
			return thisArea + thatArea;
		}
	}
	
//	public static void main(String[] args) {
//		Tree tree1 = new Tree(0, 0);
//		tree1.samplingRadiusM = 10d;
//		Tree tree2 = new Tree(8, 0);
//		tree2.samplingRadiusM = 1d;
//		double areaM2 = tree1.getCommonSamplingAreaM2(tree2);
//		int u = 0;
//	}
	
}

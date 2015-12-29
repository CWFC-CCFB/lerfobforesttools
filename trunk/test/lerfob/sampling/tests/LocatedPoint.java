package lerfob.sampling.tests;

public abstract class LocatedPoint {

	private final double xCoordM;
	private final double yCoordM;

	protected LocatedPoint(double xCoordM, double yCoordM) {
		this.xCoordM = xCoordM;
		this.yCoordM = yCoordM;
	}
	
	public double getXCoordM() {
		return xCoordM;
	}
	
	public double getYCoordM() {
		return yCoordM;
	}
	
	public double getDistanceM(LocatedPoint point) {
		double diffX = getXCoordM() - point.getXCoordM();
		double diffY = getYCoordM() - point.getYCoordM();
		double distance = Math.sqrt(diffX*diffX + diffY*diffY);
		return distance;
	}
		
	
	
}

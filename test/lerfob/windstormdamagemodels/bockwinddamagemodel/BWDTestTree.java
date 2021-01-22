package lerfob.windstormdamagemodels.bockwinddamagemodel;

import lerfob.windstormdamagemodels.bockwinddamagemodel.BWDTree;

public class BWDTestTree implements BWDTree {

	private double treeHeight;
	private double treeDbh;
	private double cHorizonDepth;
	private double crownLength;
	private double crownRadius;
	
	protected BWDTestTree(double treeHeight, double treeDbh, double cHorizonDepth, double crownLength, double crownRadius) {
		this.treeHeight = treeHeight;
		this.treeDbh = treeDbh;
		this.cHorizonDepth = cHorizonDepth;
		this.crownLength = crownLength;
		this.crownRadius = crownRadius;
	}
	

	@Override
	public double getDbhCm() {return treeDbh;}


	@Override
	public double getHeightM() {return treeHeight;}


	@Override
	public double getCrownLengthM() {return crownLength;}


	@Override
	public double getCrownRadiusM() {return crownRadius;}


	@Override
	public double getSoilHorizonCDepthM() {return cHorizonDepth;}

}

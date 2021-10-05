package lerfob.windstormdamagemodels.bockwinddamagemodel;


public class BWDTestStand implements BWDStand {

	private double dominantHeight;
	private boolean compactClay;
	private boolean rock;
	private boolean flatLand;
	
	protected BWDTestStand(double dominantHeight,
			boolean compactClay,
			boolean rock,
			boolean flatLand) {
		this.dominantHeight = dominantHeight;
		this.compactClay = compactClay;
		this.rock = rock;
		this.flatLand = flatLand;
	}
		

	@Override
	public double getDominantHeightM() {return dominantHeight;}


	@Override
	public boolean isCompactClayInFirst50cm() {return compactClay;}

	@Override
	public boolean isRockInTheFirst50cm() {return rock;}

	@Override
	public boolean isFlatLand() {return flatLand;}

}

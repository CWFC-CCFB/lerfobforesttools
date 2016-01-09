package lerfob.carbonbalancetool;

class CarbonToolCompatibleTreeImpl implements CarbonToolCompatibleTree, Cloneable {

	
	private final double number;
	private final double volM3;
	private final String speciesName;
	private StatusClass statusClass;
	
	protected CarbonToolCompatibleTreeImpl(double number, double volM3, String speciesName) {
		this.number = number;
		this.volM3 = volM3;
		this.speciesName = speciesName;
		setStatusClass(StatusClass.alive);
	}

	protected CarbonToolCompatibleTreeImpl(double volM3, String speciesName) {
		this(1d, volM3, speciesName);
	}

	@Override
	public double getNumber() {return number;}

/*	@Override
	public TreeStatusPriorToLogging getTreeStatusPriorToLogging() {return TreeStatusPriorToLogging.Alive;}
*/
	@Override
	public double getCommercialVolumeM3() {return volM3;}

	@Override
	public String getSpeciesName() {return speciesName;}

	@Override
	public void setStatusClass(StatusClass statusClass) {this.statusClass = statusClass;}

	@Override
	public StatusClass getStatusClass() {return statusClass;}

	@Override
	public SpeciesType getSpeciesType() {return SpeciesType.BroadleavedSpecies;}

	@Override
	public CarbonToolCompatibleTree clone() {
		return new CarbonToolCompatibleTreeImpl(number, volM3, speciesName);
	}
	
}

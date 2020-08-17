package lerfob.carbonbalancetool;

import lerfob.carbonbalancetool.CATSettings.CATSpecies;

class CarbonToolCompatibleTreeImpl implements CATCompatibleTree, Cloneable {

	
	private final double number;
	private final double volM3;
	private final String speciesName;
	private StatusClass statusClass;
	private final CATSpecies species;
	
	protected CarbonToolCompatibleTreeImpl(double number, double volM3, String speciesName) {
		this.number = number;
		this.volM3 = volM3;
		this.speciesName = speciesName;
		this.species = CATSpecies.getCATSpeciesFromThisString(speciesName);
		setStatusClass(StatusClass.alive);
	}

	protected CarbonToolCompatibleTreeImpl(double volM3, String speciesName) {
		this(1d, volM3, speciesName);
	}

	@Override
	public double getNumber() {return number;}

	@Override
	public double getCommercialUnderbarkVolumeM3() {return volM3;}

	@Override
	public String getSpeciesName() {return speciesName;}

	@Override
	public void setStatusClass(StatusClass statusClass) {this.statusClass = statusClass;}

	@Override
	public StatusClass getStatusClass() {return statusClass;}

	@Override
	public CATCompatibleTree clone() {
		return new CarbonToolCompatibleTreeImpl(number, volM3, speciesName);
	}

	@Override
	public CATSpecies getCATSpecies() {
		return species;
	}

}

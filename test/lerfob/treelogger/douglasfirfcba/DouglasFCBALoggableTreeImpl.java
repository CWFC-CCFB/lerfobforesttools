package lerfob.treelogger.douglasfirfcba;

import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;

class DouglasFCBALoggableTreeImpl implements DouglasFCBALoggableTree {

	final double treeDbhCm;
	
	DouglasFCBALoggableTreeImpl(double treeDbhCm) {
		this.treeDbhCm = treeDbhCm;
	}
	

	@Override
	public double getCommercialVolumeM3() {
		return 1;
	}

	@Override
	public String getSpeciesName() {
		return Species.DouglasFir.name();
	}

	@Override
	public double getDbhCm() {
		return treeDbhCm;
	}


	@Override
	public double getBarkProportionOfWoodVolume(SpeciesLocale locale) {
		return REpiceaSpecies.Species.Pseudotsuga_menziesii.getBarkProportionOfWoodVolume(locale);
	}

	@Override
	public SpeciesLocale getSpeciesLocale() {return SpeciesLocale.IPCC;}



}

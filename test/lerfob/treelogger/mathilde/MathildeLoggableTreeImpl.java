package lerfob.treelogger.mathilde;

import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.species.REpiceaSpecies.SpeciesLocale;

class MathildeLoggableTreeImpl implements MathildeLoggableTree {


	@Override
	public double getCommercialVolumeM3() {
		return 1;
	}

	@Override
	public String getSpeciesName() {
		return getMathildeTreeSpecies().name();
	}

	@Override
	public MathildeTreeSpecies getMathildeTreeSpecies() {
		return MathildeTreeSpecies.QUERCUS;
	}

	@Override
	public double getDbhCm() {
		return 50d;
	}

	@Override
	public double getBarkProportionOfWoodVolume(SpeciesLocale locale) {
		return REpiceaSpecies.Species.Quercus_spp.getBarkProportionOfWoodVolume(locale);
	}

	@Override
	public SpeciesLocale getSpeciesLocale() {
		return SpeciesLocale.IPCC;
	}

}

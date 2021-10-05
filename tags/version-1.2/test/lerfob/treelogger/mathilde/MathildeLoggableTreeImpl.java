package lerfob.treelogger.mathilde;

import repicea.simulation.species.REpiceaSpecies;

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
	public double getBarkProportionOfWoodVolume() {
		return REpiceaSpecies.Species.Quercus_spp.getBarkProportionOfWoodVolume();
	}

}

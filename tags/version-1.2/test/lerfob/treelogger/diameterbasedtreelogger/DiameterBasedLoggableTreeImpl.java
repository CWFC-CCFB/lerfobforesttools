package lerfob.treelogger.diameterbasedtreelogger;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.treelogger.LoggableTree;

class DiameterBasedLoggableTreeImpl implements LoggableTree, DbhCmProvider {

	final double dbhCm;
	
	DiameterBasedLoggableTreeImpl(double dbhCm) {
		this.dbhCm = dbhCm;
	}
	
	@Override
	public double getCommercialVolumeM3() {return 1d;}

	@Override
	public String getSpeciesName() {return REpiceaSpecies.Species.Pinus_pinaster.toString();}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getBarkProportionOfWoodVolume() {
		return REpiceaSpecies.Species.Pinus_pinaster.getBarkProportionOfWoodVolume();
	}

	@Override
	public boolean isCommercialVolumeOverbark() {
		return true;
	}


}

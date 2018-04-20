package lerfob.predictor.thinners.frenchnfithinner2018;

import repicea.simulation.covariateproviders.standlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.standlevel.StemDensityHaProvider;

public interface FrenchNFIThinnerPlot extends BasalAreaM2HaProvider,
												StemDensityHaProvider,
												SlopeInclinationPercentProvider {
	public enum Composition {
		BroadleavedDominated,
		ConiferousDominated,
		Mixed;
	}
	
	
	/**
	 * This method returns the composition of the plots based on basal area. Plots
	 * with 75% or more of their basal area in broadleaved species are considered 
	 * broadleaved dominated. Plots with 75% or more of their basal area in coniferous
	 * species are considered coniferous dominated. Plots that do fall into these
	 * two categories are considered as mixed stands. 
	 * @return a Composition enum variable
	 */
	public Composition getComposition();
	
	/**
	 * This method returns true if any silvicultural treatment was carried out in the
	 * plot during the last five years.
	 * @return a boolean
	 */
	public boolean wasThereAnySiliviculturalTreatmentInTheLast5Years();

}

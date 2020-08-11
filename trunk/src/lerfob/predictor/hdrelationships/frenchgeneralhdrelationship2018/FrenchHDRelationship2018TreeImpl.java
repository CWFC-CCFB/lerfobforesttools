/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018;

import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.volume.frenchcommercialvolume2014.FrenchCommercialVolume2014Tree;
import repicea.simulation.HierarchicalLevel;

/**
 * The FrenchHDRelationship2018TreeImpl class is a basic implementation of the
 * FrenchHDRelationship2018Tree. It facilitates the connection to JNI.
 * @author Mathieu Fortin - December 2018
 */
public class FrenchHDRelationship2018TreeImpl implements FrenchHDRelationship2018Tree, FrenchCommercialVolume2014Tree {

	private static final Map<FrenchHdSpecies, FrenchCommercialVolume2014TreeSpecies> SpeciesMatchMap = new HashMap<FrenchHdSpecies, FrenchCommercialVolume2014TreeSpecies>();
	static {
		SpeciesMatchMap.put(FrenchHdSpecies.ALISIER_BLANC, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.ALISIER_TORMINAL, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.ARBOUSIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.AUBEPINE_MONOGYNE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.AULNE_GLUTINEUX, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.BOULEAU_VERRUQUEUX, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.CHARME, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.CHATAIGNIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.CHENE_LIEGE, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		SpeciesMatchMap.put(FrenchHdSpecies.CHENE_PEDONCULE, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		SpeciesMatchMap.put(FrenchHdSpecies.CHENE_PUBESCENT, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		SpeciesMatchMap.put(FrenchHdSpecies.CHENE_ROUGE, FrenchCommercialVolume2014TreeSpecies.QUERCUS_RUBRA);
		SpeciesMatchMap.put(FrenchHdSpecies.CHENE_SESSILE, FrenchCommercialVolume2014TreeSpecies.QUERCUS_PETRAEA);
		SpeciesMatchMap.put(FrenchHdSpecies.CHENE_TAUZIN, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		SpeciesMatchMap.put(FrenchHdSpecies.CHENE_VERT, FrenchCommercialVolume2014TreeSpecies.QUERCUS_ROBUR);
		SpeciesMatchMap.put(FrenchHdSpecies.DOUGLAS, FrenchCommercialVolume2014TreeSpecies.PSEUDOTSUGA_MENZIESII);
		SpeciesMatchMap.put(FrenchHdSpecies.EPICEA_COMMUN, FrenchCommercialVolume2014TreeSpecies.PICEA_ABIES);
		SpeciesMatchMap.put(FrenchHdSpecies.EPICEA_DE_SITKA, FrenchCommercialVolume2014TreeSpecies.PICEA_ABIES);
		SpeciesMatchMap.put(FrenchHdSpecies.ERABLE_A_FEUILLES_D_OBIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.ERABLE_CHAMPETRE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.ERABLE_DE_MONTPELLIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.ERABLE_SYCOMORE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.FRENE_COMMUN, FrenchCommercialVolume2014TreeSpecies.FRAXINUS_EXCELSIOR);
		SpeciesMatchMap.put(FrenchHdSpecies.HETRE, FrenchCommercialVolume2014TreeSpecies.FAGUS_SYLVATICA);
		SpeciesMatchMap.put(FrenchHdSpecies.HOUX, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.MELEZE_D_EUROPE, FrenchCommercialVolume2014TreeSpecies.LARIX_DECIDUA);
		SpeciesMatchMap.put(FrenchHdSpecies.MERISIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.NOISETIER_COUDRIER, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.ORME_CHAMPETRE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.PEUPLIER_CULTIVE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.PIN_A_CROCHETS, FrenchCommercialVolume2014TreeSpecies.PINUS_HALEPENSIS);
		SpeciesMatchMap.put(FrenchHdSpecies.PIN_D_ALEP, FrenchCommercialVolume2014TreeSpecies.PINUS_HALEPENSIS);
		SpeciesMatchMap.put(FrenchHdSpecies.PIN_LARICIO_DE_CORSE, FrenchCommercialVolume2014TreeSpecies.PINUS_LARICIO);
		SpeciesMatchMap.put(FrenchHdSpecies.PIN_MARITIME, FrenchCommercialVolume2014TreeSpecies.PINUS_PINASTER);
		SpeciesMatchMap.put(FrenchHdSpecies.PIN_NOIR_D_AUTRICHE, FrenchCommercialVolume2014TreeSpecies.PINUS_NIGRA);
		SpeciesMatchMap.put(FrenchHdSpecies.PIN_SYLVESTRE, FrenchCommercialVolume2014TreeSpecies.PINUS_SYLVESTRIS);
		SpeciesMatchMap.put(FrenchHdSpecies.ROBINIER_FAUX_ACACIA, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.SAPIN_PECTINE, FrenchCommercialVolume2014TreeSpecies.ABIES_ALBA);
		SpeciesMatchMap.put(FrenchHdSpecies.SAULE_CENDRE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.SAULE_MARSAULT, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.SORBIER_DES_OISELEURS, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.TILLEUL_A_GRANDES_FEUILLES, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.TILLEUL_A_PETITES_FEUILLES, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
		SpeciesMatchMap.put(FrenchHdSpecies.TREMBLE, FrenchCommercialVolume2014TreeSpecies.CARPINUS_BETULUS);
	}
	
	
	
	
	protected double heightM;
	protected double dbhCm;
	protected final FrenchHdSpecies species;
	protected final FrenchHDRelationship2018PlotImpl plot;
	protected final int id;
	protected Double gOther;

	/**
	 * Constructor.
	 * @param heightM the height of the tree (m) or -1 if it was not observed
	 * @param dbhCm the diameter at breast height (cm)
	 * @param gOther the basal area of other trees (m2/ha)
	 * @param speciesName a String that corresponds to a FrenchHd2018Species enum
	 * @param plot a FrenchHDRelationship2018StandImpl instance that hosts the tree
	 */
	protected FrenchHDRelationship2018TreeImpl(int id,
			double heightM, 
			double dbhCm, 
			Double gOther, 
			String speciesName, 
			FrenchHDRelationship2018PlotImpl plot) {
		this.id = id;
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = gOther;
		this.species = FrenchHdSpecies.getFrenchHdSpeciesFromThisString(speciesName);
		this.plot = plot;
		plot.addTree(this); 
	}

	/**
	 * Constructor for NFI data. The basal area of other trees is calculated using the plot basal area.
	 * @param id an integer
	 * @param heightM the height of the tree (m) or -1 if it was not observed
	 * @param dbhCm the diameter at breast height (cm)
	 * @param speciesName a String that corresponds to a FrenchHd2018Species enum
	 * @param plot a FrenchHDRelationship2018StandImpl instance that hosts the tree
	 */
	public FrenchHDRelationship2018TreeImpl(int id,
			double heightM, 
			double dbhCm, 
			String speciesName, 
			FrenchHDRelationship2018PlotImpl plot) {
		this.id = id;
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = null;
		this.species = FrenchHdSpecies.getFrenchHdSpeciesFromThisString(speciesName);
		this.plot = plot;
		plot.addTree(this); 
	}

	/**
	 * Constructor for NFI data. The basal area of other trees is calculated using the plot basal area.
	 * The model is used in a deterministic manner. Tree heights are assumed to be all unobserved.
	 * @param dbhCm the diameter at breast height (cm)
	 * @param speciesName a String that corresponds to a FrenchHd2018Species enum
	 * @param plot a FrenchHDRelationship2018StandImpl instance that hosts the tree
	 */
	public FrenchHDRelationship2018TreeImpl(double dbhCm, 
			String speciesName, 
			FrenchHDRelationship2018PlotImpl plot) {
		this.id = 0;
		this.heightM = -1d;
		this.dbhCm = dbhCm;
		this.gOther = null;
		this.species = FrenchHdSpecies.getFrenchHdSpeciesFromThisString(speciesName);
		this.plot = plot;
		plot.addTree(this); 
	}
	
	@Override
	public int getErrorTermIndex() {
		return 0;
	}

	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return null;
	}

	@Override
	public String getSubjectId() {return ((Integer) id).toString();}

	@Override
	public int getMonteCarloRealizationId() {return plot.getMonteCarloRealizationId();}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getLnDbhCmPlus1() {return Math.log(getDbhCm() + 1);}

	@Override
	public double getSquaredLnDbhCmPlus1() {
		double lnDbhCm = getLnDbhCmPlus1();
		return lnDbhCm * lnDbhCm;
	}

	@Override
	public FrenchHdSpecies getFrenchHDTreeSpecies() {return species;}

	protected double getGOther() {return gOther;}
	protected double getBasalAreaM2() {return getSquaredDbhCm() * Math.PI * 0.000025;}

	@Override
	public double getSquaredDbhCm() {
		return getDbhCm() * getDbhCm();
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.TREE;
	}

	@Override
	public FrenchCommercialVolume2014TreeSpecies getFrenchCommercialVolume2014TreeSpecies() {
		return SpeciesMatchMap.get(getFrenchHDTreeSpecies());
	}


}

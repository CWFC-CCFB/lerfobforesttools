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
package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The FrenchHDRelationship2018PlotImpl class is a basic implementation of the
 * FrenchHDRelationship2018Plot. 
 * @author Mathieu Fortin - December 2018
 */
public class FrenchHDRelationship2018PlotImpl implements FrenchHDRelationship2018Plot {

	private int monteCarloId;
	private double mqdCm;
	private double basalAreaM2Ha;
	private boolean hasBeenHarvestedInLast5Years;
	
	protected final String id;
	protected final double pent2;
	protected final double xCoord;
	protected final double yCoord;
	protected final List<FrenchHDRelationship2018Tree> treeList;

	/**
	 * Constructor.
	 * @param id the id of the plot
	 * @param pentInc the inclination of the slope in % (for 1% it is 1 and not .01)
	 * @param xCoord the latitude in geographic coordinates
	 * @param yCoord the longitude in geographic coordinates
	 * @param hasBeenHarvestedInLast5Years a boolean that takes the value of true if the plot was managed in the last five years
	 * @param monteCarloId the Monte Carlo id if needed
	 * @param mqdCm the mean quadratic diameter of the plot (cm)
	 */
	public FrenchHDRelationship2018PlotImpl(String id, 
			double pentInc, 
			double xCoord, 
			double yCoord,
			boolean hasBeenHarvestedInLast5Years,
			int monteCarloId,
			double mqdCm) {
		this.id = id;
		this.pent2 = pentInc;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		treeList = new ArrayList<FrenchHDRelationship2018Tree>();
		
		this.hasBeenHarvestedInLast5Years = hasBeenHarvestedInLast5Years;
		this.mqdCm = mqdCm;
		this.monteCarloId = monteCarloId;
	}
	
	/**
	 * Constructor for French NFI. 
	 * @param id the id of the plot
	 * @param pentInc the inclination of the slope in % (for 1% it is 1 and not .01)
	 * @param xCoord the latitude in geographic coordinates
	 * @param yCoord the longitude in geographic coordinates
	 * @param hasBeenHarvestedInLast5Years a boolean that takes the value of true if the plot was managed in the last five years
	 * @param basalAreaM2Ha plot basal area (m2/ha)
	 * @param mqdCm plot mean quadratic diameter (cm)
	 */
	public FrenchHDRelationship2018PlotImpl(String id, 
			double pentInc, 
			double xCoord, 
			double yCoord,
			boolean hasBeenHarvestedInLast5Years,
			double basalAreaM2Ha,
			double mqdCm) {
		this.id = id;
		this.pent2 = pentInc;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		treeList = new ArrayList<FrenchHDRelationship2018Tree>();
		
		this.hasBeenHarvestedInLast5Years = hasBeenHarvestedInLast5Years;
		this.basalAreaM2Ha = basalAreaM2Ha;
		this.mqdCm = mqdCm;
	}

	
	@Override
	public String getSubjectId() {return id;}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloId;}

	@Override
	public double getMeanQuadraticDiameterCm() {return mqdCm;}

	@Override
	public boolean isInterventionResult() {return hasBeenHarvestedInLast5Years;}

	@Override
	public double getSlopeInclinationPercent() {return pent2;}

	@Override
	public double getLatitudeDeg() {return yCoord;}

	@Override
	public double getLongitudeDeg() {return xCoord;}

	@Override
	public double getElevationM() {return 0;}

	@Override
	public double getBasalAreaM2HaMinusThisSubject(FrenchHDRelationship2018Tree tree) {
		FrenchHDRelationship2018TreeImpl t = (FrenchHDRelationship2018TreeImpl) tree;
		if (t.gOther == null) {
			return basalAreaM2Ha - t.getBasalAreaM2() * 14.14; 
		} else {
			return t.getGOther();
		}
	}

	@Override
	public Collection<FrenchHDRelationship2018Tree> getTreesForFrenchHDRelationship() {return treeList;}

	protected void addTree(FrenchHDRelationship2018TreeImpl tree) {
		treeList.add(tree);
	}

}

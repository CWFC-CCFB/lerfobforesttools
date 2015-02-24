package lerfob.biomassmodel;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lerfob.biomassmodel.BiomassPredictionModel.BiomassCompartment;
import lerfob.fagacees.FagaceesSpeciesProvider.FgSpecies;

import org.junit.Test;

import repicea.serial.xml.XmlDeserializer;
import repicea.util.ObjectUtility;

public class BiomassPredictionModelTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void predictBiomassForASingleTreeTest() throws Exception {
		String referenceFilename = ObjectUtility.getPackagePath(this.getClass()) + "refList.xml";
		BiomassCompatibleTree tree = new BiomassCompatibleTreeImpl(35d, 30d, 150, FgSpecies.BEECH);
		BiomassPredictionModel model = new BiomassPredictionModel();
		Map<String, Double> outputMap = new HashMap<String,Double>();
		for (BiomassCompartment compartment : model.getCompartments()) {
			outputMap.put(compartment.toString(), model.getBiomass_kg(compartment.toString(), tree));
		}
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			XmlSerializer serializer = new XmlSerializer(referenceFilename);
//			serializer.writeObject(outputMap);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
	
		System.out.println("Loading reference map...");
		Map<String, Double> refMap = null;
		try {
			XmlDeserializer deserializer = new XmlDeserializer(referenceFilename);
			refMap = (HashMap) deserializer.readObject();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}


		assertEquals(refMap.size(), outputMap.size());

		for (String comp : refMap.keySet()) {
			double currentValue = outputMap.get(comp);
			double refValue = refMap.get(comp);
			System.out.println("Compartment: " + comp + "; Expected: " + refValue + "; Actual: " + currentValue);
			assertEquals(refValue, currentValue, 1E-8);
		}
	}	
}

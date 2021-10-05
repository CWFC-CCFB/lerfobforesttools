package lerfob.carbonbalancetool.biomassparameters;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import repicea.util.ObjectUtility;

@SuppressWarnings("deprecation")
public class BiomassParametersTest {
	
	@Test
	public void deserializationTest() {
		String filename = ObjectUtility.getPackagePath(getClass()) + "AsInNationalReporting.bpf";
		BiomassParameters bp = new BiomassParameters();
		try {
			bp.load(filename);
		} catch (IOException e) {
			Assert.fail("Deserialization of " + filename + " failed!");
		}
	}
	
}

package lerfob.app;

import org.junit.Assert;
import org.junit.Test;

import repicea.app.AbstractAppVersionCompiler;
import repicea.util.ObjectUtility;

public class LERFOBJARSVNAppVersionCompiler extends AbstractAppVersionCompiler {

	private static final String APP_URL = "https://svn.code.sf.net/p/lerfobforesttools/code/trunk";
	private static String Version_Filename = ObjectUtility.getPackagePath(LERFOBJARSVNAppVersionCompiler.class).replace("bin", "src") + LERFOBJARSVNAppVersion.ShortFilename;
	
	public LERFOBJARSVNAppVersionCompiler() {
		super();
	}
	
	@Test
	public void createRevisionFile() {
		LERFOBJARSVNAppVersionCompiler compiler = new LERFOBJARSVNAppVersionCompiler();
		try {
			compiler.createRevisionFile(APP_URL, Version_Filename);
			System.out.println("Revision file successfully updated!");
		} catch (Exception e) {
			Assert.fail("Failed to compile revision number");
		}
	}

}

package junittest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({	junittest.LanguageSettings.class,
				lerfob.app.LERFOBJARSVNAppVersionCompiler.class,
				lerfob.biomassmodel.BiomassPredictionModelTest.class,
				lerfob.carbonbalancetool.CarbonAccountingToolTest.class,
				lerfob.carbonbalancetool.productionlines.ProductionLinesTest.class,
				lerfob.carbonbalancetool.biomassparameters.BiomassParametersTest.class,
				junittest.windstormdamagemodels.aws.AWSBeechTest.class,	
				junittest.windstormdamagemodels.aws.AWSTest.class,
				junittest.windstormdamagemodels.bwd.BWDTest.class,				
				lerfob.nutrientmodel.NutrientConcentrationPredictionModelTest.class,
				lerfob.predictor.mathilde.diameterincrement.MathildeDiameterIncrementTest.class,
				lerfob.predictor.mathilde.mortality.MathildeMortalityTest.class,
				lerfob.predictor.mathilde.MathildeClimatePredictorTest.class,
				lerfob.predictor.frenchgeneralhdrelationship2014.FrenchHDRelationship2014PredictorTest.class,
				lerfob.predictor.frenchcommercialvolume2014.FrenchCommercialVolume2014PredictorTest.class})
public class AllLERFoBTests {}
package junittest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({	junittest.LanguageSettings.class,
				lerfob.carbonbalancetool.CarbonAccountingToolTest.class,
				lerfob.carbonbalancetool.productionlines.ProductionLinesTest.class,
				lerfob.carbonbalancetool.biomassparameters.BiomassParametersTest.class,
				junittest.windstormdamagemodels.aws.AWSBeechTest.class,	
				junittest.windstormdamagemodels.aws.AWSTest.class,
				junittest.windstormdamagemodels.bwd.BWDTest.class,				
				lerfob.predictor.mathilde.diameterincrement.MathildeDiameterIncrementTest.class,
				lerfob.predictor.mathilde.mortality.MathildeMortalityTest.class,
				lerfob.predictor.mathilde.climate.MathildeClimatePredictorTest.class,
				lerfob.predictor.mathilde.thinning.MathildeThinningPredictorTest.class,
				lerfob.predictor.frenchgeneralhdrelationship2014.FrenchHDRelationship2014PredictorTest.class,
				lerfob.predictor.frenchgeneralhdrelationship2018.FrenchHDRelationship2018PredictorTest.class,
				lerfob.predictor.frenchcommercialvolume2014.FrenchCommercialVolume2014PredictorTest.class,
				lerfob.treelogger.mathilde.MathildeTreeLoggerTests.class,
				lerfob.treelogger.douglasfirfcba.DouglasFCBATreeLoggerTests.class,
				lerfob.carbonbalancetool.pythonaccess.PythonAccessTests.class,
				lerfob.predictor.dopalep.DopalepDbhIncPredictorTest.class,
				lerfob.predictor.mathilde.recruitment.MathildeRecruitmentTest.class,
				lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictorTests.class,
				lerfob.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerTests.class,
				lerfob.treelogger.maritimepine.MaritimePineBasicTreeLoggerTests.class})
public class AllLERFoBTests {}

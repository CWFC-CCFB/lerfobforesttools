package junittest;

import lerfob.carbonbalancetool.catdiameterbasedtreelogger.ComparisonWithMathildeTest;
import lerfob.carbonbalancetool.pythonaccess.PythonAccessTest;
import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictorTest;
import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLoggerTest;
import lerfob.treelogger.douglasfirfcba.DouglasFCBATreeLoggerTest;
import lerfob.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerTest;
import lerfob.treelogger.maritimepine.MaritimePineBasicTreeLoggerTest;
import lerfob.treelogger.mathilde.MathildeTreeLoggerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({	junittest.LanguageSettings.class,
				lerfob.carbonbalancetool.CarbonAccountingToolTest.class,
				lerfob.carbonbalancetool.productionlines.ProductionLinesTest.class,
				lerfob.carbonbalancetool.biomassparameters.BiomassParametersTest.class,
				PythonAccessTest.class,
				ComparisonWithMathildeTest.class,
				junittest.windstormdamagemodels.aws.AWSBeechTest.class,	
				junittest.windstormdamagemodels.aws.AWSTest.class,
				junittest.windstormdamagemodels.bwd.BWDTest.class,				
				lerfob.predictor.mathilde.diameterincrement.MathildeDiameterIncrementTest.class,
				lerfob.predictor.mathilde.mortality.MathildeMortalityTest.class,
				lerfob.predictor.mathilde.climate.MathildeNewClimatePredictorTest.class,
				lerfob.predictor.mathilde.climate.formerversion.MathildeClimatePredictorTest.class,
				lerfob.predictor.mathilde.thinning.MathildeThinningPredictorTest.class,
				lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2014.FrenchHDRelationship2014PredictorTest.class,
				lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018.FrenchHDRelationship2018PredictorTest.class,
				lerfob.predictor.volume.frenchcommercialvolume2014.FrenchCommercialVolume2014PredictorTest.class,
				lerfob.predictor.volume.frenchcommercialvolume2020.FrenchCommercialVolume2020PredictorTest.class,
				MathildeTreeLoggerTest.class,
				DouglasFCBATreeLoggerTest.class,
				lerfob.predictor.dopalep.DopalepDbhIncPredictorTest.class,
				lerfob.predictor.mathilde.recruitment.MathildeRecruitmentTest.class,
				FrenchNFIThinnerPredictorTest.class,
				EuropeanBeechBasicTreeLoggerTest.class,
				MaritimePineBasicTreeLoggerTest.class,
				DiameterBasedTreeLoggerTest.class,
				})
public class AllLERFoBTests {}

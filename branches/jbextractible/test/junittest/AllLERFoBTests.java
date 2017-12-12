package junittest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({	junittest.LanguageSettings.class,
				lerfob.carbonbalancetool.CarbonAccountingToolTest.class,
				lerfob.carbonbalancetool.productionlines.ProductionLinesTest.class,
				lerfob.carbonbalancetool.biomassparameters.BiomassParametersTest.class})
public class AllLERFoBTests {}

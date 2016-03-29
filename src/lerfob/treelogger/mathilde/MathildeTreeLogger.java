package lerfob.treelogger.mathilde;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;

public class MathildeTreeLogger extends TreeLogger<MathildeTreeLoggerParameters, MathildeLoggableTree> {


	@Override
	public void setTreeLoggerParameters() {
		MathildeTreeLoggerParameters params = createDefaultTreeLoggerParameters();
		params.showInterface(null);
		setTreeLoggerParameters(params);
	}

	@Override
	public MathildeTreeLoggerParameters createDefaultTreeLoggerParameters() {
		MathildeTreeLoggerParameters params = new MathildeTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		return params;
	}

	@Override
	public MathildeLoggableTree getEligible(LoggableTree t) {
		if (t instanceof MathildeLoggableTree) {
			return (MathildeLoggableTree) t;
		} else {
			return null;
		}
	}

	@Override
	protected void logThisTree(MathildeLoggableTree tree) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		MathildeTreeLogger treeLogger = new MathildeTreeLogger();
		MathildeTreeLoggerParameters params = treeLogger.createDefaultTreeLoggerParameters();
		params.showInterface(null);
	}

}

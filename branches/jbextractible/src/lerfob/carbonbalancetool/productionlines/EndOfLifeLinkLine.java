package lerfob.carbonbalancetool.productionlines;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

import repicea.simulation.processsystem.Processor;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;
import repicea.simulation.processsystem.ValidProcessorLinkLine;

@SuppressWarnings("serial")
public class EndOfLifeLinkLine extends ValidProcessorLinkLine {

	protected static final Stroke EndOfLifeLinkLineStrokeBold = new BasicStroke(3, 
			BasicStroke.CAP_SQUARE, 
			BasicStroke.JOIN_MITER,
			1, 
			new float[]{12,12},
			0);

	protected static final Stroke EndOfLifeLinkLineStrokeDefault = new BasicStroke(1, 
			BasicStroke.CAP_SQUARE, 
			BasicStroke.JOIN_MITER,
			1, 
			new float[]{12,12},
			0);


	protected EndOfLifeLinkLine(SystemPanel panel, Processor fatherProcessor, Processor sonProcessor) {
		super(panel, fatherProcessor.getUI(panel), sonProcessor.getUI(panel));

		((ProductionLineProcessor) fatherProcessor).disposedToProcessor = sonProcessor;
		
		ProductionLineProcessorButton fatherButton = (ProductionLineProcessorButton) fatherProcessor.getUI(panel);
		fatherButton.addComponentListener(this);
		fatherButton.createEndOfLifeLinkRecognizer.setComponent(null); // disable the drag & drop
		sonProcessor.getUI(panel).addComponentListener(this);
	}

	@Override
	protected ProcessorButton getFatherAnchor() {return (ProcessorButton) super.getFatherAnchor();}

	@Override
	protected ProcessorButton getSonAnchor() {return (ProcessorButton) super.getSonAnchor();}

	@Override
	protected void finalize() {
		super.finalize();
		ProductionLineProcessor fatherProcessor =  (ProductionLineProcessor) getFatherAnchor().getOwner();
		fatherProcessor.disposedToProcessor = null;
	}

	
	@Override
	protected void setStroke(Graphics2D g2) {
		if (isSelected()) {
			g2.setStroke(EndOfLifeLinkLineStrokeBold);
		} else {
			g2.setStroke(EndOfLifeLinkLineStrokeDefault);
		}
	}

	

}

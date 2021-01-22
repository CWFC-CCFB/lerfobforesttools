package lerfob.carbonbalancetool;

import java.awt.Color;
import java.util.Random;

import javax.swing.JDialog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class CarbonViewerTestingGUI {

	
	
	protected static ChartPanel createChart() {

		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();
		Random random = new Random();
		dataset.addValue(random.nextDouble(), "Serie1",	"");
		dataset.addValue(random.nextDouble(), "Serie2",	"");
		dataset.addValue(random.nextDouble(), "Serie3",	"");
		
		JFreeChart chart = ChartFactory.createBarChart("Fake graph", 
				"Serie", 
				"Value",
				dataset, 
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.BLACK);
		BarRenderer renderer = (BarRenderer) plot.getRenderer();

		renderer.setShadowVisible(true);
		renderer.setMaximumBarWidth(0.1);

		ChartPanel chartPanel = new ChartPanel(chart);
		return chartPanel;
	}

	
	
	public static void main(String[] args) {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.add(createChart());
		dialog.setVisible(true);
		System.exit(0);
	}
	
}

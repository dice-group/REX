package org.aksw.rex.experiments;

import java.util.List;

import model.Page;
import model.Rule;

import org.slf4j.LoggerFactory;

public class QualityEvaluator {
	private org.slf4j.Logger log = LoggerFactory.getLogger(QualityEvaluator.class);
	
	private Rule result;
	private Rule golden;
	private List<Page> pages;
	private double precision;
	private double recall;

	public QualityEvaluator(Rule result, Rule golden, List<Page> pages) {
		this.result = result;
		this.golden = golden;
		this.pages = pages;
	}

	public void evaluate() {
		double tp = 0;
//		double tpWrong = 0;
		double fp = 0;
		double fn = 0;
//		double dKnow = 0;

		for (Page p : pages) {
			String goldenValue = this.golden.applyOn(p).getTextContent();
			String resultValue = this.result.applyOn(p).getTextContent();
			
			if (resultValue.trim().equals("") || goldenValue.trim().equals("")) {
				if (resultValue.trim().equals("") && goldenValue.trim().equals(""))
					// both null
					log.info("Both rules null values in page: "+p.getTitle());
				else if (resultValue.trim().equals("")){
					log.info("Not correct: it was " + resultValue.trim() + " = but it should be " + goldenValue +" in page: "+p.getTitle());
					// golden not null
					fn++;
				}
				else{
					// only golden null
					log.info("Not correct: it was " + resultValue.trim() + " = but it should be " + goldenValue+" in page: "+p.getTitle());
					fp++;
				}
			} else {
				if (resultValue.trim().equals(goldenValue.trim())) {
					tp++;
				} else {
//					tpWrong++;
					log.info("Not correct: it was " + resultValue.trim() + " = but it should be " + goldenValue+" in page: "+p.getTitle());
				}
			}
		}

		this.setPrecision(tp / (tp + fp));
		this.setRecall(tp / (tp + fn));
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getF() {
		return 2*(this.precision*this.recall)/(this.precision+this.recall);
	}
}

package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;

public class TemporalAttributeForMixEventTimeExtractor implements
RelationFeaturesExtractor {

	@SuppressWarnings("null")
	@Override
	public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
		ArrayList<Feature> feats = new ArrayList<>();
		EventMention event = null;
		TimeMention time = null;

		if(arg1 instanceof EventMention){
			event = JCasUtil.selectCovering(jCas, EventMention.class, arg1.getBegin(), arg1.getEnd()).get(0);
			if( event != null && event.getEvent() !=null ){
				feats.add(new Feature("Arg1-Event-Modality", event.getEvent().getProperties().getContextualModality()));
			}
		}else if(arg1 instanceof TimeMention){
			time = (TimeMention) arg1;
			if( time != null){
				feats.add(new Feature("Arg1-Timex-", time.getTimeClass()));
			}			
		}

		if(arg2 instanceof TimeMention){
			time = (TimeMention) arg2;
			if(time != null){
				feats.add(new Feature("Arg2-Timex-", time.getTimeClass()));
			}
		}else if(arg2 instanceof EventMention){
			event = JCasUtil.selectCovering(jCas, EventMention.class, arg2.getBegin(), arg2.getEnd()).get(0);
			if(event !=null && event.getEvent() !=null ){
				feats.add(new Feature("Arg2-Event-Modality", event.getEvent().getProperties().getContextualModality()));
			}
		}

		return feats;
	}

}
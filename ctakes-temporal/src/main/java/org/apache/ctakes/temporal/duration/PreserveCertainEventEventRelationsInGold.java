package org.apache.ctakes.temporal.duration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Preserve only those event-event relations whose both event arguments have duration data.
 */
public class PreserveCertainEventEventRelationsInGold extends JCasAnnotator_ImplBase {                                               

  public static final String GOLD_VIEW_NAME = "GoldView";

  @Override                                                                                                                  
  public void process(JCas jCas) throws AnalysisEngineProcessException {                                                     

    Map<String, Map<String, Float>> textToDistribution = null;                                                                 
    try {                                                                                                                      
      textToDistribution = Files.readLines(new File(Utils.durationDistributionPath), Charsets.UTF_8, new Utils.Callback());                                    
    } catch(IOException e) {                                                                                                   
      e.printStackTrace();                                                                                                     
      return;                                                                                                                  
    }  

    JCas goldView;                                                                                                           
    try {                                                                                                                    
      goldView = jCas.getView(GOLD_VIEW_NAME);                                                                               
    } catch (CASException e) {                                                                                               
      throw new AnalysisEngineProcessException(e);                                                                           
    }                                                                                                                                                                                                                                         

    // remove relations where one or both arguments have no duration data
    for(BinaryTextRelation relation : Lists.newArrayList(JCasUtil.select(goldView, BinaryTextRelation.class))) {            
      RelationArgument arg1 = relation.getArg1();                                                                             
      RelationArgument arg2 = relation.getArg2(); 

      String event1Text;
      String event2Text;
      if(arg1.getArgument() instanceof EventMention && arg2.getArgument() instanceof EventMention) {
        event1Text = Utils.getText(jCas, arg1.getArgument());
        event2Text = Utils.getText(jCas, arg2.getArgument());
      } else {
        // this is not an event-event relation
        continue;
      }

      if(textToDistribution.containsKey(event1Text) && textToDistribution.containsKey(event2Text)) {
        // we have duration distributions for both arguments, so keep it
        continue;
      }

      arg1.removeFromIndexes();                                                                                            
      arg2.removeFromIndexes();                                                                                            
      relation.removeFromIndexes();
    }

    // remove events (that didn't participate in relations) that have no data
    for(EventMention mention : Lists.newArrayList(JCasUtil.select(goldView, EventMention.class))) {
      String mentionText = Utils.getText(jCas, mention);
      if(textToDistribution.containsKey(mentionText)) {
        // these are the kind we keep
        continue;
      } 

      mention.removeFromIndexes();
    }
  }
}                                                                                                                            
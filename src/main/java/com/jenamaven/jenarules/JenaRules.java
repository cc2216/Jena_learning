package com.jenamaven.jenarules;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.*;

public class JenaRules {
	String dataFile, familyRules, prefix; 
	List<Rule> rules;
	Model data;
	Reasoner reasoner;
	InfModel infmodel;
	
	public static void main(String[] args) {		
		// TODO Auto-generated method stub
		JenaRules familyTree = new JenaRules();

		Resource name = familyTree.findResource("#Lily");
		Property op = familyTree.setProperty(null);
		System.out.println("lily has relatives:");
		
		familyTree.printStatements(name, op, null);
		
	}
	
	public JenaRules(){
		dataFile = JenaRules.class.getClassLoader().getResource("family.owl").getPath(); //path of family owl
		familyRules = JenaRules.class.getClassLoader().getResource("family.rules").getPath();//path of family rules
		prefix = "http://www.semanticweb.org/ontologies/2010/0/family.owl"; //main namespace
		
		//setup the reasoner with rules
		this.rules = Rule.rulesFromURL(familyRules);
		this.data = FileManager.get().loadModel(dataFile);
		this.reasoner = new GenericRuleReasoner(rules);
		this.infmodel = ModelFactory.createInfModel(reasoner, data);
		this.infmodel.removeNsPrefix(this.prefix);
		
	};
	
	public void printStatements(Resource s, Property p, Resource o) {
	    for (StmtIterator i = this.infmodel.listStatements(s,p,o); i.hasNext(); ) {
	        Statement stmt = i.nextStatement();
	        System.out.println(" - " + PrintUtil.print(stmt));
	        System.out.println("--" + stmt.getPredicate());
	    }
	}
	
	public Property setProperty(String p) {
		if (p != null) {
			return this.data.getProperty(this.prefix + p);
		} else {
			return null;
		}
		
	}
	
	public Resource findResource(String name) {
		return this.infmodel.getResource(this.prefix + name);
	}
	
	public void validation() {
		ValidityReport validity = this.infmodel.validate();
		if (validity.isValid()) {
		    System.out.println("\nOK");
		} else {
		    System.out.println("\nConflicts");
		    for (Iterator i = validity.getReports(); i.hasNext(); ) {
		        ValidityReport.Report report = (ValidityReport.Report)i.next();
		        System.out.println(" - " + report);
		    }
		}
	}
	
}


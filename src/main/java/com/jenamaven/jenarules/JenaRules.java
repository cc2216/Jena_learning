package com.jenamaven.jenarules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
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
	private static Scanner sc;
	
	public static void main(String[] args) {		
		// TODO Auto-generated method stub
		JenaRules familyTree = new JenaRules();
		
		try {
			while(true){
				int option = readOption();
				if (option == 2) {
					break;
				} else if (option == 1) {
					System.out.println("Please input the name of rules file:"); 
					String fileName = readString();
					familyTree.addRulesFile(fileName);
				} else if(option == 3) {
					System.out.println("Using SPARQL query: ");
					familyTree.usingSPARQL();
				} else {
					System.out.println("Option is wrong! ");
				}
			}	
		} catch (IOException e){
			e.printStackTrace();
		}
		
		
		try {
			System.out.println("Please input the name of the person:");  
            String namePerson = readString();
            System.out.println("Please input the the relationship you want to query (if you want query all relatives, please entre \" all \":");
            String relationship = readString();
            Resource name = familyTree.findResource(namePerson); //set up the name of person
            Property op = familyTree.setProperty(relationship == null? null:(relationship)); // set up the relationship 
            System.out.println(namePerson + " " + (relationship == null? "all relatives are" : relationship) + ":");
            familyTree.printStatements(name, op, null);
        } catch (IOException e) {
            e.printStackTrace();
        }	
		
	}
	
	public JenaRules(){
		dataFile = JenaRules.class.getClassLoader().getResource("family.owl").getPath(); //path of family owl
		familyRules = JenaRules.class.getClassLoader().getResource("family.rules").getPath();//path of family rules
		//String familyRules2 = JenaRules.class.getClassLoader().getResource("family2.rules").getPath();//path of family rules
		prefix = "http://www.semanticweb.org/ontologies/2010/0/family.owl#"; //main namespace
		
		//setup the reasoner with rules
		this.rules = Rule.rulesFromURL(familyRules);
		this.data = FileManager.get().loadModel(dataFile);
		Reasoner owlReasoner = ReasonerRegistry.getOWLReasoner();
		this.infmodel = ModelFactory.createInfModel(owlReasoner, data);
		this.reasoner = new GenericRuleReasoner(this.rules); //customed reason by rules
		this.infmodel = ModelFactory.createInfModel(reasoner, this.infmodel);
		this.infmodel.removeNsPrefix(this.prefix);
		
	};
	
	private static String readString() throws IOException {
        String userInput;  
        sc=new Scanner(System.in);
        userInput=sc.next();
         System.out.println(userInput);
         if (userInput.equals("all")) {
        	 	return null;
        	 } else {
        		 return userInput;
        	 }
    }
	
	private static int readOption() throws IOException {
        int option;  
        sc = new Scanner(System.in);  
        System.out.println("If you want add new rules please input \'1\', input \'2\' to query the family tree by name, input \'3\' to query the family tree by SPARQL:");  
        option=sc.nextInt();  
        return option;
    }
	
	public void addRulesFile(String fileName) {
		//Add new rules file		
		String newFamilyRules = JenaRules.class.getClassLoader().getResource(fileName).getPath();//path of family rules
		List<Rule> newRules = Rule.rulesFromURL(newFamilyRules);
		Reasoner newReasoner = new GenericRuleReasoner(newRules);
		this.infmodel = ModelFactory.createInfModel(newReasoner, infmodel); //create new inference by adding new rules
		this.infmodel.removeNsPrefix(this.prefix);
	}
	
	public void printStatements(Resource s, Property p, Resource o) {
	    for (StmtIterator i = this.infmodel.listStatements(s,p,o); i.hasNext(); ) {
	        Statement stmt = i.nextStatement();
	        System.out.println(" - " + PrintUtil.print(stmt));
	        System.out.println("--" + stmt.getObject());
	    }
	}
	
	public Property setProperty(String p) {
		if (p != null) {
			return this.data.getProperty(this.prefix + p);
		} else {
			return null;
		}
		
	}
	
	public void usingSPARQL() {
		String queryString = "SELECT ?x WHERE { <"+ this.prefix + "Anna> <" + this.prefix + "isMotherOf> "+" ?x }";
		System.out.println("SPARQL query is: " + queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, this.infmodel);
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results, query);
		
		/* Output results without prefix
		while (results.hasNext()) {
	        QuerySolution soln = results.nextSolution(); 
	        String s = soln.get("x").toString();
	        StringTokenizer token = new StringTokenizer(s,"#");
	        token.nextToken();       
	        System.out.println(token.nextToken());       
		}*/
		
		qe.close();
	}
	
	public Resource findResource(String name) {
		if (name != null) {
			return this.infmodel.getResource(this.prefix + name);
		} else {
			return null;
		}
		
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


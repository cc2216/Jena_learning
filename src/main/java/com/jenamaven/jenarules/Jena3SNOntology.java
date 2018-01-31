package com.jenamaven.jenarules;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;

public class Jena3SNOntology {
	String dataFile, familyRules, prefix; 
	List<Rule> rules;
	Model data, union;
	Reasoner reasoner;
	InfModel infmodel;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Jena3SNOntology jo = new Jena3SNOntology("http://3s-web.enstb.org/ontologies/SMS/3SNOntology.owl");
		jo.addOntologyFile("http://3s-web.enstb.org/ontologies/SMS/MeasurementIndicator_SMS_Ontology.owl");
		jo.addOntologyFile("http://3s-web.enstb.org/ontologies/SMS/SportsActivity_SMS_Ontology.owl");
		jo.createInference();
		jo.usingSPARQL("");

	}
	
	public Jena3SNOntology(String url){
		Model model = ModelFactory.createDefaultModel();
		data = model.read(url); //creat owl model by URL

		prefix = "http://www.imt-atlantique.fr/3s/SmartSensor.owl#"; //main namespace
		
	}
	
	public void usingSPARQL(String s) {	
		String queryString = s;
		if(queryString.equals("")) {
			//queryString = "SELECT ?x WHERE { <"+ this.prefix + "BasicSensor>  <http://www.w3.org/2000/01/rdf-schema#comment> ?x }";
			queryString = "SELECT ?x ?y WHERE { <http://www.semanticweb.org/samya/ontologies/SportsActivity.owl#IndividualSport> ?x ?y }";
		}
		System.out.println("SPARQL query is: " + queryString);
		Query query = QueryFactory.create(queryString);
		try (QueryExecution qe = QueryExecutionFactory.create(query, this.infmodel)){
			ResultSet results = qe.execSelect();
			ResultSetFormatter.out(System.out, results, query);
			//FileOutputStream outResult = new FileOutputStream("testQueryResult.owl");
			//ResultSetFormatter.out(outResult, results, query);
			qe.close();
		}catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	

	
	public void addRulesFile(String url) {
		//Add new rules file		
		String newFamilyRules = url;
		List<Rule> newRules = Rule.rulesFromURL(newFamilyRules);
		Reasoner newReasoner = new GenericRuleReasoner(newRules);
		this.infmodel = ModelFactory.createInfModel(newReasoner, infmodel); //create new inference by adding new rules
	}
	
	public void addOntologyFile(String url) {
		//Add new ontology file		
		Model newModel = ModelFactory.createDefaultModel().read(url);
		data.add(newModel);
		System.out.println("add ontology");
	}
	
	public void createInference() {
		reasoner = ReasonerRegistry.getOWLReasoner(); //using OWL Reasoner
		try {
			FileOutputStream out= new FileOutputStream("test.owl");
			this.infmodel = ModelFactory.createInfModel(reasoner, data);
			this.infmodel.write(out, "RDF/XML");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

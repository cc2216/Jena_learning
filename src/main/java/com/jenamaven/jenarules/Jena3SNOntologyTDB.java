package com.jenamaven.jenarules;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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

public class Jena3SNOntologyTDB {

	String dataFile, familyRules, prefix, directory; ; ; 
	List<Rule> rules;
	Model data;
	Reasoner reasoner;
	InfModel infmodel;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Jena3SNOntologyTDB joTDB = new Jena3SNOntologyTDB("smsTDB");
		//can not test if it's already in TDB if we want to get all ontologies in one model
		Dataset dataset = TDBFactory.createDataset("smsTDB"); 
		dataset.begin(ReadWrite.READ);  //start a transaction
		boolean datasetEmpty = dataset.isEmpty();
		dataset.end();
		if(datasetEmpty) {
			joTDB.addOntologyFile("http://3s-web.enstb.org/ontologies/SMS/3SNOntology.owl");
			joTDB.addOntologyFile("http://3s-web.enstb.org/ontologies/SMS/MeasurementIndicator_SMS_Ontology.owl");
			joTDB.addOntologyFile("http://3s-web.enstb.org/ontologies/SMS/SportsActivity_SMS_Ontology.owl");
		}
		joTDB.usingSPARQLFromTDB("");

	}
	
	public Jena3SNOntologyTDB(String s){
		this.directory = s;
	}
	
	
	public void usingSPARQLFromTDB(String s) {
		String queryString = s;
		if(queryString.equals("")) {
			//queryString = "SELECT ?x WHERE { <"+ this.prefix + "BasicSensor>  <http://www.w3.org/2000/01/rdf-schema#comment> ?x }";
			queryString = "SELECT ?x ?y WHERE { <http://www.semanticweb.org/samya/ontologies/SportsActivity.owl#IndividualSport> ?x ?y }";
		}
		System.out.println("SPARQL query is: " + queryString);
		Dataset dataset = TDBFactory.createDataset(directory);
		Model tdb = dataset.getDefaultModel();
		dataset.begin(ReadWrite.READ);  
		//reasoner = ReasonerRegistry.getOWLReasoner(); //using OWL Reasoner
		createInference(tdb);
		Query query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, this.infmodel)) {
			ResultSet results = qexec.execSelect();
			System.out.println("After Quering by SPARQL in side...................");
			ResultSetFormatter.out(results);
			qexec.close();
			System.out.print("after print out");
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			dataset.end();
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
		Dataset dataset = TDBFactory.createDataset(directory); 
		dataset.begin(ReadWrite.WRITE);  //start a transaction
		Model tdb = dataset.getDefaultModel();
		if(dataset.isEmpty()) {
			System.out.println("init TDB");
			FileManager.get().readModel(tdb, url); 
		} else {
			Model newModel = ModelFactory.createDefaultModel().read(url);
			tdb.add(newModel);
			System.out.println("add new ontology");
		}
		
		dataset.commit();  //commit to tdb  
		dataset.end();  //end a transaction	
	}
	
	public void createInference(Model tdb) {
		this.reasoner = ReasonerRegistry.getOWLReasoner(); //using OWL Reasoner
		//Dataset dataset = TDBFactory.createDataset("jenaTDB"); 
		//dataset.begin(ReadWrite.WRITE);  //start a transaction
		//Model tdb = dataset.getDefaultModel();

		try {
			//FileOutputStream out= new FileOutputStream("test2.owl");
			this.infmodel = ModelFactory.createInfModel(reasoner, tdb);
			//System.out.print("writing in file");
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

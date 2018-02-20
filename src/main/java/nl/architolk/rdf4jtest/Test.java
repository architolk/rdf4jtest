package nl.architolk.rdf4jtest;

import java.io.File;
import java.util.List;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParser;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParserFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class Test {
  
  private static final String baseURI = "http://example.org/example/local";
  
  private static final Repository repo = new SailRepository(new MemoryStore());
  
  /*
  *   Main routine
  */
  public static void main(String[] args) {
		
    System.out.println("Let's go!");
    
    // Initialize the in-memory repository
    System.out.println("Initialize in-memory repository");
    repo.initialize();

    // Add some data to the repository;
    addSomeData();
    
    // The query to be executed
    String queryString =
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
        "PREFIX dbo: <http://dbpedia.org/ontology/> " +
        "SELECT ?city ?label (lang(?label) as ?lang) " +
        "WHERE {" +
        "  ?city a dbo:City " +
        "  SERVICE <http://dbpedia.org/sparql> {" +
        "    ?city rdfs:label ?label" +
        "  }" +
        "}";
        
    // Parse the query, and find out any service clauses
    parseTheQuery(queryString);

    // Execute the query
    executeTheQuery(queryString);
    
    // Shut down the in-memory repository
    System.out.println("Shut down");
    repo.shutDown();
    
    System.out.println("Finished.");
  }
  
  /*
  *   addSomeData, putting some data from a file 'test.ttl' into the in-memory repository
  */
  private static void addSomeData() {

    File file = new File("test.ttl");
    try {
      RepositoryConnection con = repo.getConnection();
      try {
        try {
          System.out.println("Add content to repo");
          con.add(file, baseURI, RDFFormat.TURTLE);
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
      finally {
        con.close();
      }
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
  
  /*
  *   parseTheQuery, search for any SERVICE clauses and output the SPARQL endpoint
  */
  private static void parseTheQuery(String queryString) {
    
    try {
      System.out.println("Parse query and find SERVICE clauses");
      SPARQLParserFactory spfactory = new SPARQLParserFactory();
      QueryParser queryParser = spfactory.getParser();
      ParsedQuery query = queryParser.parseQuery(queryString, baseURI);
      FederatedQueryVisitor visitor = new FederatedQueryVisitor();
      query.getTupleExpr().visit(visitor);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
  
  /*
  *   executeTheQuery, and show the results
  */
  private static void executeTheQuery(String queryString) {
    
    try {
      RepositoryConnection con = repo.getConnection();
      try {
        try {
          System.out.println("Query repo");
          
          TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
          TupleQueryResult result = tupleQuery.evaluate();
          List<String> columns = result.getBindingNames();
          for (String name : columns) {
            System.out.print("|"+name);
          }
          System.out.println();
          System.out.println("-----------------------");
          while (result.hasNext()) {  // iterate over the result
            BindingSet bindingSet = result.next();
            for (String name : columns) {
              Value value = bindingSet.getValue(name);
              System.out.print("|"+value.stringValue());
            }
            System.out.println();
          }
          System.out.println("-----------------------");
          result.close();
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
      finally {
        con.close();
        System.out.println("Connection closed");
      }
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }

  }
}
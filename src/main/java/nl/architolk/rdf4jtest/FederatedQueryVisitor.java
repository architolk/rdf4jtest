package nl.architolk.rdf4jtest;

import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

public class FederatedQueryVisitor extends AbstractQueryModelVisitor<Exception> {
  
  @Override
  public void meet(Service node) {
    System.out.println("> Service endpoint: " + node.getServiceRef().getValue());
  }
  
}
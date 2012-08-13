package conformance.autogenerated;

import org.junit.Test;

import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_description_logic_602 {
    @Test
    public void testWebOnt_description_logic_602() {
        String premise = "Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)\n"
                + "Prefix(owl:=<http://www.w3.org/2002/07/owl#>)\n"
                + "Prefix(xml:=<http://www.w3.org/XML/1998/namespace>)\n"
                + "Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)\n"
                + "Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)\n"
                + "Prefix(urn:=<urn:test#>)\n" + "Ontology(<urn:testonto:>\n"
                + "Declaration(Class(<urn:A.2>))\n"
                + "EquivalentClasses(<urn:A.2> ObjectAllValuesFrom(<urn:r> <urn:c>))\n"
                + "SubClassOf(<urn:A.2> <urn:d>)\n"
                + "Declaration(Class(<urn:Unsatisfiable>))\n"
                + "SubClassOf(<urn:Unsatisfiable> <urn:c>)\n"
                + "SubClassOf(<urn:Unsatisfiable> <urn:d.comp>)\n"
                + "Declaration(Class(<urn:c>))\n"
                + "SubClassOf(<urn:c> ObjectAllValuesFrom(<urn:r> <urn:c>))\n"
                + "Declaration(Class(<urn:d>))\n"
                + "EquivalentClasses(<urn:d> ObjectMaxCardinality(0 <urn:p>))\n"
                + "Declaration(Class(<urn:d.comp>))\n"
                + "EquivalentClasses(<urn:d.comp> ObjectMinCardinality(1 <urn:p>))\n"
                + "Declaration(ObjectProperty(<urn:r>))\n"
                + "Declaration(ObjectProperty(<urn:p>))\n"
                + "ClassAssertion(<urn:Unsatisfiable> urn:ind))";
        // "Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)\n"+
        // "Prefix(owl:=<http://www.w3.org/2002/07/owl#>)\n"+
        // "Prefix(xml:=<http://www.w3.org/XML/1998/namespace>)\n"+
        // "Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)\n"+
        // "Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)\n"+
        // "Prefix(urn:=<urn:test#>)\n"+
        //
        //
        // "Ontology(<urn:testonto:>\n"+
        //
        // "Declaration(Class(<urn:A.2>))\n"+
        // "EquivalentClasses(<urn:A.2> ObjectAllValuesFrom(<urn:r> <urn:c>))\n"+
        // "SubClassOf(<urn:A.2> <urn:d>)\n"+
        // "Declaration(Class(<urn:Unsatisfiable>))\n"+
        // "SubClassOf(<urn:Unsatisfiable> <urn:c>)\n"+
        // "SubClassOf(<urn:Unsatisfiable> <urn:d.comp>)\n"+
        // "Declaration(Class(<urn:c>))\n"+
        // "SubClassOf(<urn:c> ObjectAllValuesFrom(<urn:r> <urn:c>))\n"+
        // "Declaration(Class(<urn:d>))\n"+
        // "EquivalentClasses(<urn:d> DataMaxCardinality(0 <urn:p>))\n"+
        // "Declaration(Class(<urn:d.comp>))\n"+
        // "EquivalentClasses(<urn:d.comp> DataMinCardinality(1 <urn:p>))\n"+
        // "Declaration(ObjectProperty(<urn:r>))\n"+
        // "Declaration(DataProperty(<urn:p>))\n"+
        // "ClassAssertion(<urn:Unsatisfiable> urn:ind))";
        // "<rdf:RDF\n"
        // + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
        // + "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
        // + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        // + " xml:base=\"urn:testonto:\">\n"
        // + " <owl:Ontology rdf:about=\"\"/>\n"
        // + " <owl:Class rdf:about=\"urn:d.comp\">\n"
        // + "  <owl:equivalentClass>\n"
        // + "   <owl:Restriction>\n"
        // + "    <owl:onProperty>\n"
        // + "     <owl:DatatypeProperty rdf:about=\"urn:p\"/>\n"
        // + "    </owl:onProperty>\n"
        // + "    <owl:minCardinality rdf:datatype=\"/2001/XMLSchema#int\"\n"
        // + "    >1</owl:minCardinality>\n"
        // + "   </owl:Restriction>\n"
        // + "  </owl:equivalentClass>\n"
        // + " </owl:Class>\n"
        // + " <owl:Class rdf:about=\"urn:Unsatisfiable\">\n"
        // + "  <rdfs:subClassOf>\n"
        // + "   <owl:Class rdf:about=\"urn:c\"/>\n"
        // + "  </rdfs:subClassOf>\n"
        // + "  <rdfs:subClassOf rdf:resource=\"urn:d.comp\"/>\n"
        // + " </owl:Class>\n"
        // + " <owl:Class rdf:about=\"urn:d\">\n"
        // + "  <owl:equivalentClass>\n"
        // + "   <owl:Restriction>\n"
        // + "    <owl:onProperty rdf:resource=\"urn:p\"/>\n"
        // + "    <owl:maxCardinality rdf:datatype=\"/2001/XMLSchema#int\"\n"
        // + "    >0</owl:maxCardinality>\n"
        // + "   </owl:Restriction>\n"
        // + "  </owl:equivalentClass>\n"
        // + " </owl:Class>\n"
        // + " <owl:Class rdf:about=\"urn:c\">\n"
        // + "  <rdfs:subClassOf>\n"
        // + "   <owl:Restriction>\n"
        // + "    <owl:onProperty>\n"
        // + "     <owl:ObjectProperty rdf:about=\"urn:r\"/>\n"
        // + "    </owl:onProperty>\n"
        // + "    <owl:allValuesFrom rdf:resource=\"urn:c\"/>\n"
        // + "   </owl:Restriction>\n"
        // + "  </rdfs:subClassOf>\n"
        // + " </owl:Class>\n"
        // + " <owl:Class rdf:about=\"urn:A.2\">\n"
        // + "  <rdfs:subClassOf rdf:resource=\"urn:d\"/>\n"
        // + "  <owl:equivalentClass>\n"
        // + "   <owl:Restriction>\n"
        // + "    <owl:onProperty rdf:resource=\"urn:r\"/>\n"
        // + "    <owl:allValuesFrom rdf:resource=\"urn:c\"/>\n"
        // + "   </owl:Restriction>\n" + "  </owl:equivalentClass>\n"
        // + " </owl:Class>\n" +
        // " <owl:Thing><rdfs:type rdf:about=\"urn:Unsatisfiable\"/></owl:Thing>\n"
        // + "</rdf:RDF>";
        String conclusion = "";
        String id = "WebOnt_description_logic_602";
        TestClasses tc = TestClasses.valueOf("INCONSISTENCY");
        String d = "DL Test: fact2.1";
        JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
        r.setReasonerFactory(Factory.factory());
        // r.getConfiguration().setLoggingActive(true);
        // OWLOntology p = r.getPremise();
        // for(OWLAxiom x:p.getLogicalAxioms()) {
        // System.out
        // .println(x);
        // }
        // p.getOWLOntologyManager().saveOntology(p, new
        // OWLFunctionalSyntaxOntologyFormat(), new SystemOutDocumentTarget());
        r.run();
    }
}

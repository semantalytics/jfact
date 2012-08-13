package conformance.autogenerated;

import org.junit.Test;

import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class Different_types_in_Datatype_Restrictions_and_Complement {
    @Test
    public void testDifferent_types_in_Datatype_Restrictions_and_Complement() {
        String premise = "Prefix(:=<http://example.org/>)\n"
                + "Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)\n" + "Ontology(\n"
                + "  Declaration(NamedIndividual(:a))\n"
                + "  Declaration(DataProperty(:dp))\n" + "  Declaration(Class(:A))\n"
                + "  SubClassOf(:A DataAllValuesFrom(:dp \n"
                + "    DataOneOf(\"3\"^^xsd:integer \"4\"^^xsd:int))\n" + "  ) \n"
                + "  SubClassOf(:A DataAllValuesFrom(:dp \n"
                + "    DataOneOf(\"2\"^^xsd:short \"3\"^^xsd:integer))\n" + "  )\n"
                + "  ClassAssertion(:A :a)\n"
                + "  ClassAssertion(DataSomeValuesFrom(:dp\n"
                + "    DataComplementOf(DataOneOf(\"3\"^^xsd:integer))) :a\n" + "  )\n"
                + ")";
        String conclusion = "";
        String id = "Different_types_in_Datatype_Restrictions_and_Complement";
        TestClasses tc = TestClasses.valueOf("INCONSISTENCY");
        String d = "The individual a must have dp fillers that are in the sets {3, 4} and {2, 3} (different types are used, but shorts and ints are integers), but at the same time 3 is not allowed as a dp filler for a, which causes the inconsistency.";
        JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
        r.setReasonerFactory(Factory.factory());
        r.run();
    }
}
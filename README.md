
# Neo4jService

Neo4jService is a collection of Kotlin classes that provide common services
for applications to interact with a Neo4j 4.x database. The goal is to 
minimize redundancy for client applications. In addition to supporting
Neo4j transactions, this repository also supports additional utility
fuctions.

### Neo4j-related services
1. The **Neo4jConnectionService** class maintains a connection to a local
Neo4j 4.x database. It requires that two (2) system environment values,
**NEO4J_USER** and **NEO4J_PASSWOORD** be set to authenticate the user. This class
also has a function to execute a supplied Cypher statement and return the result.

2. The **Neo4jConstraintDefinition** class provides functions that allow
client applications to define Neo4j database constraints.

3. The **Neo4jCypherWriter** class provides support for journaling all Neo4j 
CREATE and MERGE statements executed by the application to a specified 
file.

4. The **Neo4jUtils** class provides a set of generic utility functions 
for interacting
with Neo4j. Most of these functions (*e.g.* NodeExistsPredicate) have self describing
names. A small utility function, formatPropertyValue, is especially useful
when composing Cypher commands. It will enquote a supplied String if 
the string is alphanumeric, but not if it is numeric. The returned String is thus
usable in a Cypher command.

### File-related classes
The *org.batteryparkdev.io* package contains classes that support reading
CSV and TSV files. They utilize the Apache Commons CSV file for parsing
delimited files

### Logging service
The LogService class provides a generic logging service for client
applications. Logging properties can be specified in the logback.xml file
in the resources directory.

### Placeholder nodes
A common issue in importing data into a Neo4j database is when a data
entry that will be mapped to one or more Neo4j nodes also includes references
to one or more entities. These references represent the identifiers that
will be evntually mapped to their own nodes. These anticipated nodes will
have Neo4j relationships to the nodes currently being loaded. An example
might be mapping properties for a Mutation that includes the PubMed Ids of 
publications that support the mutation properties. In Neo4j this would 
result in a Mutation - [HAS_REFERENCE] -> Publication relationship. In this
example, the properties for the Mutation node are currently available, but only
the identifier for the Publication node is present. (This example assumes
that the relevant Publication node has not been previously loaded.) It would
be more efficient to load the Mutation node and establish its relationship to 
the Publication node during a single pass through the mutation dataset. 
The solution implented in this repository is to allow for the creation of
a placeholder node (*i.e.* a node with just a label and a node 
identifier) for the Publication node. The remaining properties for this
Publication node can be set when the publication data is loaded. 

It should be noted that for Publication nodes representing PubMed articles,
the node's properties are completed by retrieving data from NCBI rather than
by processing a dataset. In that case it is useful to specify a required
property (*e.g.* article title) as an empty string. Later a simple Cypher
query, based on that property,
 can return a collection of PubMed identifiers that need to be retrieved.
 This is especially useful in retrieving data from remote sources because 
 it does not delay the impact of loading data from a local resource 
 (*e.g.* CSV file).

Placeholders are supported by two (2) Kotlin data classes: *NodeIdentifier* and
*PlaceholderNode*. A *NodeIdentifier* instance contains sufficient information 
to identifier a unique node within the database. A *PlaceholderNode* contains
two *NodeIdentifier* properties, representing the existing parent node, and
the new placeholder node, the label for their relationship, and, optionally, the 
 name of the child node property that should be blank. If the parent to child
 relationship requires properties, they need to be set using a subsequent Cypher
 command.
## Environment Variables

To run this project, you must set the following system environment variables:

`NEO4J_USER`

`NEO4J_PASSWORD`


## License

[MIT](https://choosealicense.com/licenses/mit/)


## Authors

- [@fcriscuolo](https://fcriscuo.github.io/)





## Feedback

If you have any feedback, please reach out to me at batteryparkdev@gmail.com


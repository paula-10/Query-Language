In this project, I implemented a Query Language inspired by SQL, by using a database I created. 
Additionally, I adopted the use of extension and implicits syntax to simplify function calls, providing an easy-to-understand and elegant query syntax.

===== Table Representation
Consider the example below.
Name	   Surname	Age
Popescu	  Ion	    30
Ionescu	  Maria	  25

This table can be represented as:
type Row = Map[String, String] // column_name - value
type Tabular = List[Row]

We will define a table as a class with attributes tableName and tableData.
case class Table(tableName: String, tableData: Tabular) {
  def header: List[String] = ???
  def data: Tabular = ???
  def name: String = ???
}

For working with tables, the following operations were implemented:
- toString method that returns the table in CSV format.
- insert a row into the table.
- delete all rows exactly matching the one received as a parameter.
- sort the table rows by a specific column.
- select function that takes a list of strings and returns a new Table object containing only the specified columns.
- apply function in a companion object of the Table class, which parses a string and returns a table with the given name.

==== Table Filters
<filter> ::= 
    <filter> && <filter> | 
    <filter> || <filter> |
    <filter> == <filter> |
    !<filter> |
    any [ <filter> ] |
    all [ <filter> ] |
    operation [ <filter> ]
We defined the operation of filtering table data as an ADT. This allows for defining complex filter operations composed of multiple conditions. This ADT has the following constructors:
- Field: Represents a filter condition on a table field. This condition is satisfied if the value in the specified column meets the predicate.
- Compound: Represents a filter condition composed of multiple conditions. This condition is satisfied if all conditions in the list are satisfied.
- Not: Represents the negation of a filter condition.
- And: Represents the conjunction of two filter conditions.
- Or: Represents the disjunction of two filter conditions.
- Equal: Represents an equality condition between two filter conditions.
- Any: Represents a filter condition that is satisfied if at least one condition in the list is satisfied.
- All: Represents a filter condition that is satisfied if all conditions in the list are satisfied.
To simplify the definition of filter conditions, I defined several operators for more concise code. The following operators that extend the FilterCond class were used:
===: To check the equality of two filter conditions.
&&: To perform the conjunction of two filter conditions.
||: To perform the disjunction of two filter conditions.
!!: To negate a filter condition.

extension (f: FilterCond) {
  def ===(other: FilterCond) = ???
  def &&(other: FilterCond) = ???
  def ||(other: FilterCond) = ???
  def !! = ???
}

Additionally, I abstracted the instantiation of a Field object, allowing the use of a tuple of the form (String, String ⇒ Boolean) to create a Field object. 
We defined the operation of filtering table rows that meet a specific condition and the operation of updating a row in the table. The latter receives as 
input a condition that dictates the rows to be modified. The updated values are found in a Map[column_name, new_value].

===== Operations with One or More Tables
A database contains multiple tables, on which we can apply a series of operations:

create: Creates a new table with a unique name and a list of columns.
drop: Deletes an existing table.
selectTables: Extracts a subset of tables from the existing list of tables.
join: Combines two tables based on a common key.

===== Query Language
I developed a query language that will serve as an API for a wide range of table transformations, previously implemented as functions. This query language will allow 
sequences or combinations of these transformations.

In implementing the query language, we will focus on including functionalities similar to those in SQL and error handling. The language will allow two main categories of operations:

Operations on the entire database.
Operations on a single table.
For error handling, we will use the Option ADT, where Some(_) indicates a valid operation result, while None signals an error. If a query generates an error, it will 
propagate if the result is needed in executing another query.

We defined the operations that can be performed on a database using the PP_SQL_DB ADT. The eval function should call the corresponding methods defined in Database.

scala
Copy code
trait PP_SQL_DB {
  def eval: Option[Database]
}

case class CreateTable(database: Database, tableName: String) extends PP_SQL_DB {
  def eval: Option[Database] = ???
}

case class DropTable(database: Database, tableName: String) extends PP_SQL_DB {
  def eval: Option[Database] = ???
}

case class SelectTables(database: Database, tableNames: List[String]) extends PP_SQL_DB {
  def eval: Option[Database] = ???
}

case class JoinTables(database: Database, table1: String, column1: String, table2: String, column2: String) extends PP_SQL_DB {
  def eval: Option[Database] = ??? // convention: returns a Database containing a single table
}
We defined the operations that can be performed on a table using the PP_SQL_Table ADT. The eval function should call the corresponding methods defined in Table.

scala
Copy code
trait PP_SQL_Table {
  def eval: Option[Table]
}

case class InsertRow(table: Table, values: Tabular) extends PP_SQL_Table {
  def eval: Option[Table] = ???
}

case class UpdateRow(table: Table, condition: FilterCond, updates: Map[String, String]) extends PP_SQL_Table {
  def eval: Option[Table] = ???
}

case class SortTable(table: Table, column: String) extends PP_SQL_Table {
  def eval: Option[Table] = ???
}

case class DeleteRow(table: Table, row: Row) extends PP_SQL_Table {
  def eval: Option[Table] = ???
}

case class FilterRows(table: Table, condition: FilterCond) extends PP_SQL_Table {
  def eval: Option[Table] = ???
}

case class SelectColumns(table: Table, columns: List[String]) extends PP_SQL_Table {
  def eval: Option[Table] = ???
}
Note: The user wants to have a more readable syntax for this Query Language. Therefore, we will define implicits for each of the eval operations of these two ADTs, 
where the operation is a string:

CreateTable - "CREATE"
DropTable - "DROP"
SelectTables - "SELECT"
JoinTables - "JOIN"
InsertRow - "INSERT"
UpdateRow - "UPDATE"
SortTable - "SORT"
DeleteRow - "DELETE"
FilterRows - "FILTER"
SelectColumns - "EXTRACT"

type Row = Map[String, String]
type Tabular = List[Row]

case class Table (tableName: String, tableData: Tabular) {
  
  override def toString: String = {
    val header = tableData.headOption.map(_.keys.toList).getOrElse(List.empty)
    val rows = tableData.map(_.values.toList)
    val allRows = header +: rows

    allRows.foldLeft("") { (acc, row) =>
      if (acc.isEmpty) acc + row.mkString(",")
      else acc + "\n" + row.mkString(",")
    }
  }
  
  def insert(row: Row): Table = {
    if (!tableData.contains(row)) Table(tableName, tableData :+ row)
    else Table(tableName, tableData)
  }
  
  def delete(row: Row): Table = Table(tableName, tableData.filter(_ != row))
  
  def sort(column: String): Table = {
    val sortedData = tableData.sortWith { (row1, row2) =>
      val value1 = row1.getOrElse(column, "")
      val value2 = row2.getOrElse(column, "")
      value1 < value2
    }
    Table(tableName, sortedData)
  }

  def update(f: FilterCond, updates: Map[String, String]): Table = {
    val updatedData = tableData.map { row =>
      if (f.eval(row).getOrElse(false)) {
        val updatedRow = row ++ updates
        updatedRow
      } else {
        row
      }
    }
    Table(tableName, updatedData)
  }
  
  def filter(f: FilterCond): Table = {
    val filteredData = tableData.filter(row => f.eval(row).getOrElse(false))
    Table(tableName, filteredData)
  }
  
  def select(columns: List[String]): Table = {
    val selectedData = tableData.map(row => row.view.filterKeys(columns.contains).toMap)
    Table(tableName, selectedData)
  }

  def header: List[String] = tableData.headOption.map(_.keys.toList).getOrElse(List.empty)
  def data: Tabular = tableData
  def name: String = tableName
}

object Table {
  def apply(name: String, s: String): Table = {
    val rows = s.split("\n").toList
    if (rows.isEmpty) {
      Table(name, List.empty)
    } else {
      val header = rows.head.split(",").toList
      val data = rows.drop(1).flatMap(row => {
        val values = row.split(",").toList
        if (values.length == header.length) {
          Some(header.zip(values).toMap)
        } else {
          None
        }
      })
      Table(name, data)
    }
  }
}

extension (table: Table) {
  def todo(i: Int): Table = {
    val newTableData = table.tableData.drop(i)
    Table(table.tableName, newTableData)
  }
}

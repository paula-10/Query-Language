case class Database(tables: List[Table]) {
  override def toString: String = {
    val tableStrings = tables.map(_.toString).mkString("\n\n")
    val resultString = "Database:\n%s".format(tableStrings)
    resultString
  }

  def create(tableName: String): Database = {
    if (tables.exists(_.tableName == tableName)) this
    else Database(tables :+ Table(tableName, List.empty))
  }

  def drop(tableName: String): Database = {
    val newTables = tables.filter(table => !table.tableName.equals(tableName))
    Database(newTables)
  }

  def selectTables(tableNames: List[String]): Option[Database] = {
    val selectedTables = tableNames.flatMap(name => tables.find(_.tableName == name))
    if (selectedTables.length == tableNames.length) Some(Database(selectedTables))
    else None
  }

  def join(table1: String, c1: String, table2: String, c2: String): Option[Table] = {
    tables.find(_.name == table1).flatMap { t1 =>
      tables.find(_.name == table2).map { t2 =>
        val commons = t1.tableData.flatMap { row1 =>
          t2.tableData.find(row2 => row1(c1) == row2(c2)).map { row2 =>
            val combined = row1 ++ row2.view.filterKeys(_ != c2).map {
              keyValue =>
                val (key, value) = keyValue
                val newValue = if (row1.contains(key) && row2.contains(key) && row1(key) != row2(key)) {
                  row1(key).concat(";").concat(row2(key))
                } else {
                  if (value.isEmpty) row1(c1) else value
                }
                key -> newValue
            }
            combined
          }
        }

        val restOf1 = t1.tableData.filterNot(row1 => t2.tableData.exists(row2 => row1(c1) == row2(c2)))
          .map { only1 =>
            val matchingRow2 = t2.tableData.find(row2 => row2(c2) == only1(c1))
            matchingRow2 match {
              case Some(row2) =>
                only1 ++ row2.view.filterKeys(_ != c2).map { case (k, v) =>
                  k -> (if (v == "") v else "")
                }
              case None =>
                only1 ++ t2.header.filter(_ != c2).map { k =>
                  k -> (if (k == c2) only1(c1) else only1.getOrElse(k, ""))
                }
            }
          }

        val restOf2 = t2.tableData.filterNot(row2 => t1.tableData.exists(row1 => row1(c1) == row2(c2)))
          .map { only2 =>
            val updatedRow = if (only2.contains(c2)) only2 - c2 + (c1 -> only2(c2)) else only2
            val matchingRow1 = t1.tableData.find(row1 => row1(c1) == updatedRow(c1))
            matchingRow1 match {
              case Some(row1) =>
                updatedRow ++ row1.view.filterKeys(_ != c1).map { row =>
                  row._1 -> (if (row._2 == "") row._2 else "")
                }
              case None =>
                updatedRow ++ t1.header.filter(_ != c1).map { k =>
                  k -> updatedRow.getOrElse(k, "")
                }
            }
          }

        val joined = commons ++ restOf1 ++ restOf2
        val joinedData = joined.map(_.toMap)
        Table("composed_rows", joinedData)
      }
    }
}

  def apply(i : Int): Table = tables(i)
}

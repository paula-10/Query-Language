object Queries {

  def killJackSparrow(t: Table): Option[Table] = queryT(Some(t), "FILTER", Not(Field("name", _ == "Jack")))

  def insertLinesThenSort(db: Database): Option[Table] =
    Some(Table("Inserted Fellas", List.empty))
      .flatMap(table => Some(InsertRow(table, List(
        Map("name" -> "Ana", "age" -> "93", "CNP" -> "455550555"),
        Map("name" -> "Diana", "age" -> "33", "CNP" -> "255532142"),
        Map("name" -> "Tatiana", "age" -> "55", "CNP" -> "655532132"),
        Map("name" -> "Rosmaria", "age" -> "12", "CNP" -> "855532172")
      )).eval.getOrElse(table)))
      .flatMap(tableSorted => Some(SortTable(tableSorted, "age").eval.getOrElse(tableSorted)))

  def youngAdultHobbiesJ(db: Database): Option[Table] = queryDB(Some(db), "JOIN", "People", "name", "Hobbies", "name")
    .flatMap { joinedDB => joinedDB.tables.headOption.flatMap { table =>
      Some(table.filter(row =>
        Some(row("age").nonEmpty && row("age").forall(_.isDigit)))).map { filteredTable =>
        filteredTable.filter(
          Field("age", _.toInt < 25) && Field("name", _.startsWith("J")) && Field("hobby", _.nonEmpty)
        )
        }.flatMap { filteredTable =>
          queryT(Some(filteredTable), "EXTRACT", List("name", "hobby"))
      }
    }
  }

}
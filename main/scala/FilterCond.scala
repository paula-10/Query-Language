import scala.language.implicitConversions

trait FilterCond {def eval(r: Row): Option[Boolean]}

case class Field(colName: String, predicate: String => Boolean) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    r.get(colName) match {
      case Some(value) => Some(predicate(value))
      case None => None
    }
  }
}

case class Compound(op: (Boolean, Boolean) => Boolean, conditions: List[FilterCond]) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    val results = conditions.flatMap(_.eval(r))
    if (results.nonEmpty) Some(results.reduce(op))
    else None
  }
}

case class Not(f: FilterCond) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    f.eval(r) match {
      case Some(result) => Some(!result)
      case None => None
    }
  }
}

def And(f1: FilterCond, f2: FilterCond): FilterCond = Compound(_ && _, List(f1, f2))
def Or(f1: FilterCond, f2: FilterCond): FilterCond = Compound(_ || _, List(f1, f2))
def Equal(f1: FilterCond, f2: FilterCond): FilterCond = Compound(_ == _, List(f1, f2))

case class Any(fs: List[FilterCond]) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
    fs.foldLeft(Option(false)) { (accResult, condition) =>
      if (accResult.contains(true)) accResult
      else {
        val result = condition.eval(r)
        if (result.contains(true)) Some(true)
        else accResult
      }
    }
  }
}

case class All(fs: List[FilterCond]) extends FilterCond {
  override def eval(r: Row): Option[Boolean] = {
//    val results = fs.flatMap(_.eval(r))
//    if (results.nonEmpty) Some(results.forall(identity))
//    else None
    fs.foldLeft(Option(true)) { (accResult, condition) =>
      if (accResult.contains(false)) accResult
      else {
        val result = condition.eval(r)
        if (result.contains(false)) Some(false)
        else accResult
      }
    }
  }
}

implicit def tuple2Field(t: (String, String => Boolean)): Field = Field(t._1, t._2)

extension (f: FilterCond) {
  def ===(other: FilterCond) = Equal(f, other)
  def &&(other: FilterCond) = And(f, other)
  def ||(other: FilterCond) = Or(f, other)
  def !! = Not(f)
}
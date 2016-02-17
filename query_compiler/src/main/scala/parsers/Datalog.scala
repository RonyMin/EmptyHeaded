package duncecap

import scala.util.parsing.json._
import scala.io._
import scala.util.parsing.combinator.RegexParsers

case class ParserFailureException() extends Exception(s"""ParserFailureException""")

/*
 A datalog parser which parsers EmptyHeaded datalog
 into our intermediate representation (IR.scala).
*/
object DatalogParser extends RegexParsers {
  def run(line:String) : IR = {
    println("HERE PARSING DATALOG: " + line)
    val irbuilder = new IRBuilder()
    this.parseAll(this.rule, line) match {
      case DatalogParser.Success(parsedRules, _) => {
        println(parsedRules.length)
        irbuilder.addRules(parsedRules)
      } case _ => 
        throw new ParserFailureException()
    }
    irbuilder.build()
  }

  def body = """[a-z]+""".r

  def datalogRule:Parser[Rule] = body ^^ {
    case r => {
      val relR = new Rel("R",Attributes(List("a","b")),Annotations(List()))
      
      val result = new Result(relR)
      val order = Order(Attributes(List("a","b")))
      val project = Project(Attributes(List()))
      val operation = Operation("*")
      val join = Join(List(relR))
      val aggregations = Aggregations(List(new Aggregation(new SUM(),Attributes(List("a")),"1","")))
      val filters = Filters(List(new Selection("a",new EQUALS(),"1.0")))
      println("I GOT A RULE: "+r)
      new Rule(result,order,project,operation,join,aggregations,filters)
    }
  }

  def emptyStatement = "".r ^^ {case r => List()}
  def notLastRule:Parser[List[Rule]] = datalogRule ~ ("." ~> rule) ^^ {case a~rest => a +: rest}
  def lastRule:Parser[List[Rule]] = datalogRule ^^ {case a => List(a)}
  def rule:Parser[List[Rule]] = notLastRule | lastRule | emptyStatement

  def rules = rep1(rule)
}

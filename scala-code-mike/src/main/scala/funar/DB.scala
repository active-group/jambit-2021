package funar

/*
put("Mike", 15)
x = get("Mike")
put("Mike", x + 1)
y = get("Mike")
return y
*/
object DB {
  /*
  trait DBCommand
  case class Put(key: String, value: Int) extends DBCommand
  case class Get(key: String) extends DBCommand

  type DBProgram = List[DBCommand]

  val p1 = List(Put("Mike", 15), Get("Mike"))
  */

  trait DB
  case class Get(key: String, callback: Int => DB)
  case class Put(key: String, value: Int, callback: Unit => DB)
}
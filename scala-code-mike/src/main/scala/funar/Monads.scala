package funar

object Monads {
  // geht nur für Typen mit Typparameter
  trait Monad[M] {
    def pure[A](result: A): M[A]
  }

}
package funar

/*
Methode:
1. einfache Beispiele für Domänenobjekte einholen
2. die modellieren -> möglicherweise Sackgasse
3. einfache Beispiele in "atomare" Bestandteile zerlegen
4. nach Selbstreferenzen suchen
5. ggf. mit weiteren Beispiel wiederholen
6. nach binärem Operator suchen
7. wenn 6 erfolgreich: neutrales Element
*/

/*
Einfacher Vertrag:

Bekomme 100 Pfund am 29.1.2001

Bekomme 200 EUR am 31.12.2021

Bezahle 100 Pfund am 1.2.2002

Zero-Coupon Bond
Zero-Bond

3 Ideen:
- "später"
- "Währung"
- "Betrag"

*/

import Contract.Amount

sealed trait Currency
object Currency {
  case object EUR extends Currency
  case object GBP extends Currency
}

case class Date(desc: String)

sealed trait Contract
/*
case class ZeroCouponBond(amount: Amount, currency: Currency, date: Date) extends Contract
case class Everest()
case class Call()
case class Put()
case class Annapurna()
*/
case class One(currency: Currency) extends Contract // "Bekomme jetzt 1EUR"
//case class Multiple(amount: Amount, currency: Currency) extends Contract
case class Multiple(amount: Amount, contract: Contract) extends Contract
case class Later(date: Date, contract: Contract) extends Contract
// "dreht den Vertrag um"
case class Pay(contract: Contract) extends Contract
// binärer Operator, +, *, beside, overlay
case class Both(contract1: Contract, contract2: Contract) extends Contract
case object Zero extends Contract


object Contract {
   type Amount = Double

   // bekomme 100 EUR jetzt
   val contract1 = Multiple(100, One(Currency.EUR))

   // Es gelten Gleichungen, z.B. zcb1 ~~~ zcb2
   val zcb1 = Later(Date("2001-01-29"), Multiple(100, One(Currency.GBP)))
   val zcb2 = Multiple(100, Later(Date("2001-01-29"), One(Currency.GBP)))

   def zeroCouponBond(amount: Amount, currency: Currency, date: Date): Contract =
    Later(date, Multiple(amount, One(currency)))

   val zcb3 = zeroCouponBond(100, Currency.GBP, Date("2001-01-29"))

   // Pay(Pay(c)) ~~~ c
   val contract3 = Pay(Later(Date("2002-02-01"), Multiple(100, One(Currency.GBP))))

   // D1 aus dem Paper
   val d1 = Both(zcb1, contract3)

   val contract4 = Multiple(100, Zero)

   sealed trait Direction
   case object Long extends Direction
   case object Short extends Direction

   case class Payment(direction: Direction, date: Date, amount: Amount, currency: Currency)
   
   // operationelle Semantik: zeitliche Entwicklung der Domänenobjekte
   // (vs. denotationalle Semantik: Domänenobjekt auf mathematisches Objekt abbilden)

   case class ContractInProgress(contract: Contract, payment: Seq[Payment])

   // Zahlungen bis now
   def semantics(contract: Contract, now: Date): (Seq[Payment], Contract) =
      contract match {
        case Zero => (Seq.empty, Zero)
        case One(currency) =>
          (Seq(Payment(Long, now, 1, currency)), Zero)
        case Multiple(amount, contract) => ???
        case Later(date, contract) => ???
        case Pay(contract) => ???
        case Both(contract1, contract2) => ???
      }

}
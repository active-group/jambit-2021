package funar.hearts

import scala.annotation.tailrec
import funar.hearts.GameEvent.HandDealt
import funar.hearts.GameEvent.PlayerTurnChanged

object Table {
  def whoTakesTrick(trick: Trick): Player = {
    assert(!trick.isEmpty)
    @tailrec
    def loop(player: Player, card: Card, trick: Trick): Player =
      trick match {
        case Nil => player
        case ((player1, card1)::rest) =>
          card.beats(card1) match {
            case None => loop(player, card, rest)
            case Some(false) => loop(player, card, rest)
            case Some(true) => loop(player1, card1, rest)
          }
      }
    val (player0, card0) :: rest1 = trick.reverse
    loop(player0, card0, rest1)
  }

  def legalCard(card: Card, hand: Hand, trick: Trick): Boolean =
    hand.contains(card) &&
    (Trick.isEmpty(trick) ||
     {
       val firstCard = Trick.leadingCard(trick)
       val firstSuit = firstCard.suit
       (card.suit == firstSuit || hand.forall(_.suit != firstSuit))
     })

  def cardScore(card: Card): Integer =
    card match {
      case Card(Suit.Spades, Rank.Queen) => 13
      case Card(Suit.Hearts, _) => 1
      case _ => 0
    }

  // Karten, die man vom Stich aufnehmen mußte
  type Pile = Set[Card]

  type PlayerPiles = Map[Player, Pile]

  // "Make illegal states unrepresentable."
  case class TableState(players: List[Player], // erste Spieler der Liste ist dran
                        hands: PlayerHands,
                        piles: PlayerPiles,
                        trick: Trick)

  def emptyTableState(players: List[Player]): TableState =
    TableState(players = players,
               hands = Map.from(players.map((_, Hand.empty))),
               piles = Map.from(players.map((_, Set.empty))),
               trick = Trick.empty)

  def gameAtBeginning(tableState: TableState): Boolean =
    Trick.isEmpty(tableState.trick) &&
      tableState.piles.values.forall(_.isEmpty)

  def playerAfter(tableState: TableState, player: Player): Player =
    rotate(rotateTo(player, tableState.players)).head

  def rotate[A](list: List[A]): List[A] = {
    assert(!list.isEmpty)
    list.tail ++ List(list.head)
  }

  @tailrec
  def rotateTo[A](x: A, list: List[A]): List[A] = {
    assert(!list.isEmpty)
    if (list.head == x)
      list
    else
      rotateTo(x, rotate(list))
  }

  def currentPlayer(tableState: TableState): Player =
    tableState.players.head

  def playValid(tableState: TableState, player: Player, card: Card): Boolean = {
    val hand = tableState.hands(player)
    System.out.println("playValid " + tableState + " " + player + " " + hand)
    val trick = tableState.trick
    legalCard(card, hand, trick) &&
     (if (gameAtBeginning(tableState))
        card == Card(Suit.Clubs, Rank.Two)
      else
        currentPlayer(tableState) == player)
  }

  def turnOver(tableState: TableState): Boolean =
    tableState.players.length == tableState.trick.length


  def pileScore(pile: Pile): Integer =
    pile.map(cardScore).foldLeft(0)(_ + _)

  def gameOver(tableState: TableState): Option[Player] =
    if (tableState.hands.values.forall(_.isEmpty)) {
      val playerScores = tableState.piles.map { case (player, pile) => (player, pileScore(pile)) }
      Some(playerScores.minBy(_._2)._1)
    } else 
      None

  def takeCard(hands: PlayerHands, player: Player, card: Card): PlayerHands =
    hands.updatedWith(player) { o => o.map(_ - card) }

  def addToPile(playerPiles: PlayerPiles, player: Player, cards: Seq[Card]): PlayerPiles = {
    val playerPile = playerPiles.getOrElse(player, Set.empty)
    playerPiles + (player -> playerPile.union(Set.from(cards)))
  }

  def tableProcessEvent(event: GameEvent, tableState: TableState): TableState =
    event match {
      case HandDealt(player, hand) => 
        tableState.copy(hands = tableState.hands + (player -> hand),
                        trick = Trick.empty)
      case PlayerTurnChanged(player) =>
        tableState.copy(players = rotateTo(player, tableState.players))
      case GameEvent.LegalCardPlayed(player, card) =>
        tableState.copy(hands = takeCard(tableState.hands, player, card),
                        trick = Trick.add(tableState.trick, player, card))
      case GameEvent.IllegalCardPlayed(player, card) => tableState
      case GameEvent.TrickTaken(player, trick) =>
        tableState.copy(piles = addToPile(tableState.piles, player, Trick.cards(trick)),
                        trick = Trick.empty)
      case GameEvent.GameEnded(player) => tableState
    }

  def tableProcessCommand(command: GameCommand, tableState: TableState): Seq[GameEvent] = {
    import GameCommand._
    import GameEvent._
    command match {
      case DealHands(hands) =>
        hands.toSeq.map(HandDealt.tupled)
      case PlayCard(player, card) =>
        if (playValid(tableState, player, card)) {
          val event1 = LegalCardPlayed(player, card)
          val state1 = tableProcessEvent(event1, tableState)
          if (turnOver(state1)) {
            val trick = state1.trick
            val trickTaker = whoTakesTrick(trick)
            val event2 = TrickTaken(trickTaker, trick)
            val state2 = tableProcessEvent(event2, state1)
            val event3 = gameOver(state2) match {
                           case Some(winner) => GameEnded(winner)
                           case None => PlayerTurnChanged(trickTaker)
                         }
            Seq(event1, event2, event3)
          } else {
             val event2 = PlayerTurnChanged(playerAfter(state1, player))
             Seq(event1, event2)
          }
        } else 
          Seq(IllegalCardPlayed(player, card))
    }
  }

}
      

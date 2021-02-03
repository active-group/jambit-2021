module Intro where

x :: Integer
x = 42

-- Ein Haustier ist eins der folgenden:
-- - Hund
-- - Katze
-- - Schlange
-- data: neuer Datentyp
data Pet = Dog | Cat | Snake
    deriving Show

-- Großbuchstaben: "konstant"
-- Kleinbuchstaben: Variable

-- Ist Haustier niedlich?
isCute :: Pet -> Bool
isCute Dog = True
isCute Cat = True
isCute Snake = False

-- Aggregatzustand ist eins der folgenen:
-- - fest
-- - flüssig
-- - gas
data State = Solid | Liquid | Gas
  deriving Show

waterState :: Double -> State
waterState temp =
    if temp < 0
    then Solid
    else if temp < 100
    then Liquid
    else Gas

-- Ein Gürteltier:
-- - tot oder lebendig
-- - Gewicht
data Liveness = Dead | Alive
  deriving Show

data Dillo = Dillo { dilloLiveness :: Liveness, dilloWeight :: Double }
--   ^^^^Typ  ^^^^ Konstruktor
  deriving Show

-- Gürteltier, lebendig, 10kg
dillo1 :: Dillo
dillo1 = Dillo { dilloLiveness = Alive, dilloWeight = 10 }
-- Gürteltier, tot, 8kg
dillo2 :: Dillo
dillo2 = Dillo Dead 8
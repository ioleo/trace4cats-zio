package trace4cats.context.zio

import _root_.zio.ZIO
import trace4cats.context.zio.instances._
import trace4cats.context.{Lift, Unlift}

object ZIOLayeredUnliftInstanceSummonTest {
  class R()
  type E
  type Low[x] = ZIO[Any, E, x]

  type F[x] = ZIO[R, E, x]
  implicitly[Lift[Low, F]]
  implicitly[Unlift[Low, F]]

  type G[x] = ZIO[R, E, x]
  implicitly[Lift[Low, G]]
  implicitly[Unlift[Low, G]]
}

package trace4cats.context.zio

import _root_.zio.ZIO
import trace4cats.context._
import trace4cats.context.zio.instances._

// These summon tests work only on Scala 2.
// As of Scala 3.1.0, the compiler fails to provide evidence for `(R & Has[C]) <:< R1`.
// In user code the instances must be summoned explicitly via `zioProvideSome`.
object ZIOLayeredProvideInstanceSummonTest {
  class R()
  type E
  type Low[x] = ZIO[Any, E, x]

  type F[x] = ZIO[R, E, x]
  implicitly[Ask[F, R]]
  implicitly[Local[F, R]]
  implicitly[Provide[Low, F, R]]

  type G[x] = ZIO[R, E, x]
  implicitly[Ask[G, R]]
  implicitly[Local[G, R]]
  implicitly[Provide[Low, G, R]]
}

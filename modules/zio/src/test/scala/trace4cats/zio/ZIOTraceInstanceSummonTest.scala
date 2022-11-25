package trace4cats.zio

import trace4cats.Span
import trace4cats.context.Local
import trace4cats.Trace
import zio.interop.catz._
import zio.{RIO, Task, ZIO}

object ZIOTraceInstanceSummonTest {
  type Effect[+A] = Task[A]

  type F[x] = SpannedEnvRIO[Span[Effect], x]
  implicitly[Trace[F]]

  type G[x] = RIO[Span[Task], x]
  implicit val rioLayeredLocalSpan: Local[G, Span[Task]] =
    zioProvideSome[Any, Span[Task], Throwable, Span[Task]]

  implicitly[Trace[G]]

  // Lens behavior relies on the non ZLayer instances
  type H[x] = RIO[Env, x]
  implicit val rioLocalSpan: Local[H, Span[Task]] =
    Local[H, Env].focus(Env.span)
  implicitly[Trace[H]]

  type I[x] = RIO[Span[Effect], x]
  implicitly[Trace[I]].span("Hello")(ZIO.succeed("World"))
}

package trace4cats.zio

import trace4cats.Span
import zio.UIO

trait ZIOTraceInstances {
  implicit val spannedRIOTrace: SpannedRIOTracer = new SpannedRIOTracer
}

trait ZIOHasTraceInstances {
  implicit def spannedEnvRIOTrace[R <: Span[UIO[*]]]: SpannedEnvRIOTracer[R] = new SpannedEnvRIOTracer[R]
}

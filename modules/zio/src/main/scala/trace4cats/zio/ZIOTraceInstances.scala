package trace4cats.zio

//import trace4cats.Span
//import zio.Task

trait ZIOTraceInstances {
  implicit val spannedRIOTrace: SpannedRIOTracer = new SpannedRIOTracer
}

trait ZIOHasTraceInstances {
  // implicit def spannedEnvRIOTrace[R <: Span[Task[*]]]: SpannedEnvRIOTracer[R] = new SpannedEnvRIOTracer[R]
}

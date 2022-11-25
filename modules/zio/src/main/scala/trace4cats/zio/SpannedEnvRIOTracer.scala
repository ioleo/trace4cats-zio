package trace4cats.zio

import cats.syntax.show._
import trace4cats.model.{AttributeValue, SpanKind, SpanStatus, TraceHeaders}
import trace4cats.{ErrorHandler, Span, ToHeaders, Trace}
import zio.interop.catz._
import zio.{Task, ZIO}

/** For use with ZLayers
  */
class SpannedEnvRIOTracer[Env <: Span[Task[*]]] extends Trace[SpannedEnvRIO[Env, *]] {
  override def put(key: String, value: AttributeValue): SpannedEnvRIO[Env, Unit] =
    ZIO
      .service[Span[Task[*]]]
      .flatMap(_.put(key, value))

  override def putAll(fields: (String, AttributeValue)*): SpannedEnvRIO[Env, Unit] =
    ZIO
      .service[Span[Task[*]]]
      .flatMap(_.putAll(fields: _*))

  override def span[A](name: String, kind: SpanKind, errorHandler: ErrorHandler)(
    fa: SpannedEnvRIO[Env, A]
  ): SpannedEnvRIO[Env, A] = {
    for {
      env <- ZIO.environment[Env]
      span <- ZIO.service[Span[Task[*]]]
      result <- span
        .child(name, kind, errorHandler)
        .use { (childSpan: Span[Task[*]]) =>
          val deps = env.add(childSpan)
          fa.provideEnvironment(deps)
        }
    } yield result
  }

  override def headers(toHeaders: ToHeaders): SpannedEnvRIO[Env, TraceHeaders] =
    ZIO
      .service[Span[Task[*]]]
      .map(s => toHeaders.fromContext(s.context))

  override def setStatus(status: SpanStatus): SpannedEnvRIO[Env, Unit] =
    ZIO
      .service[Span[Task[*]]]
      .flatMap(_.setStatus(status))

  override def traceId: SpannedEnvRIO[Env, Option[String]] =
    ZIO.service[Span[Task[*]]].map { s =>
      Some(s.context.traceId.show)
    }
}

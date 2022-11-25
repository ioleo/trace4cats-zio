package trace4cats.zio

import cats.syntax.show._
import trace4cats.model.{AttributeValue, SpanKind, SpanStatus, TraceHeaders}
import trace4cats.{ErrorHandler, Span, ToHeaders, Trace}
import zio.interop.catz._
import zio.{RIO, Tag, Task, ZEnvironment, ZIO}

class SpannedRIOTracer extends Trace[SpannedRIO] {
  override def put(key: String, value: AttributeValue): SpannedRIO[Unit] =
    ZIO.service[Span[Task]].flatMap(_.put(key, value))

  override def putAll(fields: (String, AttributeValue)*): SpannedRIO[Unit] =
    ZIO.service[Span[Task]].flatMap(_.putAll(fields: _*))

  override def span[A](name: String, kind: SpanKind, errorHandler: ErrorHandler)(fa: SpannedRIO[A]): SpannedRIO[A] =
    ZIO.service[Span[Task]].flatMap(_.child(name, kind, errorHandler).use(s => fa.provideEnvironment(ZEnvironment(s))))

  override def headers(toHeaders: ToHeaders): SpannedRIO[TraceHeaders] =
    ZIO.service[Span[Task]].map { s =>
      toHeaders.fromContext(s.context)
    }

  override def setStatus(status: SpanStatus): SpannedRIO[Unit] =
    ZIO.service[Span[Task]].flatMap(_.setStatus(status))

  override def traceId: SpannedRIO[Option[String]] =
    ZIO.service[Span[Task]].map { s =>
      Some(s.context.traceId.show)
    }

  def lens[R: Tag](f: R => Span[Task], g: (R, Span[Task]) => R): Trace[RIO[R, *]] =
    new Trace[RIO[R, *]] {
      override def put(key: String, value: AttributeValue): RIO[R, Unit] =
        ZIO.service[R].flatMap { r =>
          f(r).put(key, value)
        }

      override def putAll(fields: (String, AttributeValue)*): RIO[R, Unit] =
        ZIO.service[R].flatMap { r =>
          f(r).putAll(fields: _*)
        }

      override def span[A](name: String, kind: SpanKind, errorHandler: ErrorHandler)(fa: RIO[R, A]): RIO[R, A] =
        ZIO.service[R].flatMap { r =>
          f(r).child(name, kind, errorHandler).use(s => fa.provideEnvironment(ZEnvironment(g(r, s))))
        }

      override def headers(toHeaders: ToHeaders): RIO[R, TraceHeaders] =
        ZIO.service[R].flatMap { r =>
          ZIO.succeed(toHeaders.fromContext(f(r).context))
        }

      override def setStatus(status: SpanStatus): RIO[R, Unit] =
        ZIO.service[R].flatMap { r =>
          f(r).setStatus(status)
        }

      override def traceId: RIO[R, Option[String]] =
        ZIO.service[R].flatMap { r =>
          ZIO.succeed(Some(f(r).context.traceId.show))
        }
    }
}

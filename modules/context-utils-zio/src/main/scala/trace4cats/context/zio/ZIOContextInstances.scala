package trace4cats.context.zio

import _root_.zio.{interop, IO, Tag, ZEnvironment, ZIO, ZLayer}
import cats.{~>, Monad}
import trace4cats.context.{Provide, Unlift}

trait ZIOContextInstances extends ZIOContextInstancesLowPriority {
  implicit def zioProvide[E, R: Tag]: Provide[IO[E, *], ZIO[R, E, *], R] =
    new Provide[IO[E, *], ZIO[R, E, *], R] {
      def Low: Monad[IO[E, *]] = interop.catz.monadErrorInstance
      def F: Monad[ZIO[R, E, *]] = interop.catz.monadErrorInstance

      def ask[R2 >: R]: ZIO[R, E, R2] = ZIO.service
      def local[A](fa: ZIO[R, E, A])(f: R => R): ZIO[R, E, A] = fa.provideSomeEnvironment(_.update(f))
      def lift[A](la: IO[E, A]): ZIO[R, E, A] = la
      def provide[A](fa: ZIO[R, E, A])(r: R): IO[E, A] = fa.provideEnvironment(ZEnvironment(r))

      override def access[A](f: R => A): ZIO[R, E, A] = ZIO.environmentWith(env => f(env.get))
      override def accessF[A](f: R => ZIO[R, E, A]): ZIO[R, E, A] = ZIO.environmentWithZIO(env => f(env.get))
      override def kleislift[A](f: R => IO[E, A]): ZIO[R, E, A] = ZIO.environmentWithZIO(env => f(env.get))
    }

  implicit def zioUnliftSome[R, R1 <: R, E]: Unlift[ZIO[R, E, *], ZIO[R1, E, *]] =
    new Unlift[ZIO[R, E, *], ZIO[R1, E, *]] {
      def Low: Monad[ZIO[R, E, *]] = interop.catz.monadErrorInstance
      def F: Monad[ZIO[R1, E, *]] = interop.catz.monadErrorInstance

      def lift[A](la: ZIO[R, E, A]): ZIO[R1, E, A] = la
      def askUnlift: ZIO[R1, E, ZIO[R1, E, *] ~> ZIO[R, E, *]] =
        ZIO.environmentWith[R1](r1 =>
          new (ZIO[R1, E, *] ~> ZIO[R, E, *]) {
            def apply[A](fa: ZIO[R1, E, A]): ZIO[R, E, A] = fa.provideEnvironment(r1)
          }
        )
    }
}

@scala.annotation.nowarn
trait ZIOContextInstancesLowPriority {
  implicit def zioProvideSome[R: Tag, R1: Tag, E, C: Tag](implicit
    ev1: R1 <:< R with C,
    ev2: R with C <:< R1
  ): Provide[ZIO[R, E, *], ZIO[R1, E, *], C] =
    new Provide[ZIO[R, E, *], ZIO[R1, E, *], C] {
      def Low: Monad[ZIO[R, E, *]] = interop.catz.monadErrorInstance
      def F: Monad[ZIO[R1, E, *]] = interop.catz.monadErrorInstance

      def ask[C2 >: C]: ZIO[R1, E, C2] = ZIO.service[C].provideSomeLayer[R1](ZLayer.succeed(ev1))
      def local[A](fa: ZIO[R1, E, A])(f: C => C): ZIO[R1, E, A] =
        fa.provideSomeEnvironment[R1](_.update[R1](f.asInstanceOf[R1 => R1]))

      def lift[A](la: ZIO[R, E, A]): ZIO[R1, E, A] = la.provideSomeLayer[R1](ZLayer.succeed(ev1))
      def provide[A](fa: ZIO[R1, E, A])(c: C): ZIO[R, E, A] = fa.provideSomeLayer[R](ZLayer.succeed(c))
    }
}

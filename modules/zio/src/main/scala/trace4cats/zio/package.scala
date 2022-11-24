package trace4cats

import _root_.zio.{RIO, Task, UIO}
import trace4cats.context.zio.ZIOContextInstances

package object zio extends ZIOTraceInstances with ZIOHasTraceInstances with ZIOContextInstances {
  // For use with ZLayers
  type SpannedEnvRIO[-R <: Span[UIO[*]], +A] = RIO[R, A]

  // For use with Tagless Final style/no ZLayers
  type SpannedRIO[+A] = RIO[Span[Task], A]
}

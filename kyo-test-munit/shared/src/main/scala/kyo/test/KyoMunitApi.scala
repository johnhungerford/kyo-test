package kyo.test

import kyo.*
import munit.*
import scala.concurrent.Future

trait KyoMunitApi extends KyoTestApiSync[Unit] with KyoTestApiAsync[Future[Unit]] with KyoTestApiSpecialAssertion[Any]:
    self: Suite & Assertions =>

    type Assert = KyoAssert.Assert

    override inline def assertKyo(inline condition: Boolean)(using Frame): Unit < Assert =
        KyoAssert.get(assert(condition))

    override def assertKyo(assertion: => Any)(using f: Frame): Unit < Assert =
        KyoAssert.get(assertion)

    override def runKyoSync(effect: Any < (Assert & Memo & Abort[Any] & Sync))(using Frame): Unit =
        import AllowUnsafe.embrace.danger

        effect.handle(
            KyoAssert.run,
            Memo.run,
            Abort.run[Any](_)
        ).map {
            case Result.Success(_)              => ()
            case Result.Failure(thr: Throwable) => Sync.defer(throw thr)
            case Result.Failure(e)              => Sync.defer(fail(t"Test failed with Abort: $e".toString))
            case Result.Panic(thr)              => Sync.defer(throw thr)
        }.handle(
            Sync.Unsafe.evalOrThrow
        )
    end runKyoSync

    override def runKyoAsync(effect: Any < (Assert & Memo & Scope & Abort[Any] & Async))(using Frame): Future[Unit] =
        import AllowUnsafe.embrace.danger

        effect.handle(
            KyoAssert.run,
            Scope.run,
            Memo.run,
            Abort.run
        ).map {
            case Result.Success(_)              => ()
            case Result.Failure(thr: Throwable) => Sync.defer(throw thr)
            case Result.Failure(e)              => Sync.defer(fail(t"Test failed with Abort: $e".toString))
            case Result.Panic(thr)              => Sync.defer(throw thr)
        }.handle(
            Fiber.init(_),
            _.map(_.toFuture),
            Sync.Unsafe.evalOrThrow
        )
    end runKyoAsync
end KyoMunitApi

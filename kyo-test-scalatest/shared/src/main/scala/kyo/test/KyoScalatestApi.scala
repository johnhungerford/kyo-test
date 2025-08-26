package kyo.test

import kyo.*
import org.scalatest.Assertion
import org.scalatest.Suite
import scala.concurrent.Future

trait KyoScalatestApi extends KyoTestApiSync[Assertion] with KyoTestApiAsync[Future[Assertion]] with KyoTestApiSpecialAssertion[Assertion]:
    self: Suite =>

    type Assert = KyoAssert.Assert

    override inline def assertKyo(inline condition: Boolean)(using Frame): Unit < Assert =
        KyoAssert.get(assert(condition))

    override def assertKyo(assertion: => Assertion)(using f: Frame): Unit < Assert =
        KyoAssert.get(assertion)

    override def runKyoSync(effect: Any < (Assert & Choice & Memo & Abort[Any] & Sync))(using Frame): Assertion =
        import AllowUnsafe.embrace.danger

        effect.handle(
            KyoAssert.run,
            Choice.run,
            Memo.run,
            Abort.run[Any](_)
        ).map {
            case Result.Success(_)              => succeed
            case Result.Failure(thr: Throwable) => Sync.defer(throw thr)
            case Result.Failure(e)              => Sync.defer(fail(t"Test failed with Abort: $e".toString))
            case Result.Panic(thr)              => Sync.defer(throw thr)
        }.handle(
            Sync.Unsafe.evalOrThrow
        )
    end runKyoSync

    override def runKyoAsync(effect: Any < (Assert & Choice & Memo & Scope & Abort[Any] & Async))(using Frame): Future[Assertion] =
        import AllowUnsafe.embrace.danger

        effect.handle(
            KyoAssert.run,
            Choice.run,
            Scope.run,
            Memo.run,
            Abort.run
        ).map {
            case Result.Success(_)              => succeed
            case Result.Failure(thr: Throwable) => Sync.defer(throw thr)
            case Result.Failure(e)              => Sync.defer(fail(t"Test failed with Abort: $e".toString))
            case Result.Panic(thr)              => Sync.defer(throw thr)
        }.handle(
            Fiber.init(_),
            _.map(_.toFuture),
            Sync.Unsafe.evalOrThrow
        )
    end runKyoAsync
end KyoScalatestApi

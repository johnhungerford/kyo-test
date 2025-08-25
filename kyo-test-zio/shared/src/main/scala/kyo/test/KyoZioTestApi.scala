package kyo.test

import kyo.*
import zio.ZIO
import zio.test.TestResult
import zio.test.ZIOSpecAbstract
import zio.test.assertTrue

trait KyoZioTestApi extends KyoTestApiSync[Either[Any, TestResult]] with KyoTestApiAsync[ZIO[Any, Any, TestResult]]
    with KyoTestApiSpecialAssertion[TestResult]:
    self: ZIOSpecAbstract =>

    type Assert = KyoAssert.Assert

    override inline def assertKyo(inline condition: Boolean)(using Frame): Unit < Assert =
        KyoAssert.get(assertTrue(condition))

    override def assertKyo(assertion: => TestResult)(using f: Frame): Unit < Assert =
        KyoAssert.get(assertion)

    override def runKyoSync(effect: Any < (Assert & Memo & Abort[Any] & Sync))(using Frame): Either[Any, TestResult] =
        import AllowUnsafe.embrace.danger

        effect.handle(
            KyoAssert.run,
            Memo.run,
            Abort.run
        ).map {
            case Result.Success(v) => Right(v)
            case Result.Failure(e) => Left(e)
            case Result.Panic(thr) => Left(thr)
        }.handle(
            Sync.Unsafe.evalOrThrow
        )
    end runKyoSync

    def runKyoSyncFailFast(effect: Any < (Assert & Memo & Abort[Any] & Sync))(using Frame): Either[Any, TestResult] =
        import AllowUnsafe.embrace.danger

        effect.handle(
            KyoAssert.runFailingFast,
            Memo.run,
            Abort.run
        ).map {
            case Result.Success(v) => Right(v)
            case Result.Failure(e) => Left(e)
            case Result.Panic(thr) => Left(thr)
        }.handle(
            Sync.Unsafe.evalOrThrow
        )
    end runKyoSyncFailFast

    override def runKyoAsync(effect: Any < (Assert & Memo & Scope & Abort[Any] & Async))(using Frame): ZIO[Any, Any, TestResult] =
        ZIOs.run:
            effect.handle(
                KyoAssert.run,
                Scope.run,
                Memo.run
            )
    end runKyoAsync

    def runKyoAsyncFailFast(effect: Any < (Assert & Memo & Scope & Abort[Any] & Async))(using Frame): ZIO[Any, Any, TestResult] =
        ZIOs.run:
            effect.handle(
                KyoAssert.runFailingFast,
                Scope.run,
                Memo.run
            )
    end runKyoAsyncFailFast
end KyoZioTestApi

package kyo.test

import kyo.*
import munit.FailExceptionLike

private[test] object KyoAssert:
    opaque type Assert = Abort[Failure] & Sync

    sealed private[test] case class Failure(inner: FailExceptionLike[?] & Throwable)

    def run[A, S](effect: A < (Assert & Sync & S))(using ConcreteTag[Failure], Frame): A < (Sync & S) =
        Abort.runPartial[Failure](effect).map:
            case Result.Success(a)   => a
            case Result.Failure(exc) => Sync.defer(throw exc.inner)

    inline def get(inline block: => Any)(using Frame): Unit < Assert =
        Sync.defer:
            try
                val _ = block
            catch
                case e: FailExceptionLike[?] => Abort.fail(Failure(e))
                case _                       => ()

end KyoAssert

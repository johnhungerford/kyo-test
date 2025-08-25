package kyo.test

import kyo.*
import utest.AssertionError

private[test] object KyoAssert:
    opaque type Assert = Abort[Failure] & Sync

    sealed private[test] case class Failure(inner: AssertionError)

    def run[A, S](effect: A < (Assert & Sync & S))(using ConcreteTag[Failure], Frame): A < (Sync & S) =
        Abort.runPartial[Failure](effect).map:
            case Result.Success(a)   => a
            case Result.Failure(exc) => Sync.defer(throw exc.inner)

    inline def get(inline block: => Any)(using Frame): Unit < Assert =
        Sync.defer:
            try
                val _ = block
            catch
                case e: AssertionError => Abort.fail(Failure(e))
                case _                 => ()

end KyoAssert

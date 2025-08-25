package kyo.test

import kyo.*
import org.scalatest.exceptions.TestCanceledException
import org.scalatest.exceptions.TestFailedException

private[test] object KyoAssert:
    opaque type Assert = Abort[Failure] & Sync

    sealed private[test] case class Failure(inner: TestFailedException | TestCanceledException)

    def run[A, S](effect: A < (Assert & Sync & S))(using ConcreteTag[Failure], Frame): A < (Sync & S) =
        Abort.runPartial[Failure](effect).map:
            case Result.Success(a)   => a
            case Result.Failure(exc) => Sync.defer(throw exc.inner)

    inline def get(inline block: => org.scalatest.Assertion)(using Frame): Unit < Assert =
        Sync.defer:
            try
                val _ = block
            catch
                case e: (TestFailedException | TestCanceledException) => Abort.fail(Failure(e))
                case _                                                => ()

end KyoAssert

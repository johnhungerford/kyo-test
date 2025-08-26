package kyo.test

import kyo.*

trait KyoTestApiBase:
    type Assert
    inline def assertKyo(inline assertion: Boolean)(using Frame): Unit < Assert
end KyoTestApiBase

trait KyoTestApiSpecialAssertion[Assertion]:
    self: KyoTestApiBase =>
    def assertKyo(assertion: => Assertion)(using Frame): Unit < Assert

trait KyoTestApiSync[TestResultSync] extends KyoTestApiBase:
    def runKyoSync(effect: Any < (Assert & Choice & Memo & Abort[Any] & Sync))(using Frame): TestResultSync

trait KyoTestApiAsync[TestResultAsync] extends KyoTestApiBase:
    def runKyoAsync(effect: Any < (Assert & Choice & Memo & Scope & Abort[Any] & Async))(using Frame): TestResultAsync

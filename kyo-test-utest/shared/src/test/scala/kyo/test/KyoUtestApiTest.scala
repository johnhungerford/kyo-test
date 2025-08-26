package kyo.test

import kyo.{test as ktest, *}
import utest.*

class KyoUtestApiSyncTest extends TestSuite with KyoUtestApi:
    val tests = Tests {
        test("sync"):
            runKyoSync:
                val varEffect =
                    for
                        i  <- Var.get[Int]
                        _  <- Var.update[Int](_ + 1)
                        i2 <- Var.get[Int]
                        _  <- assertKyo(assertMatch(i2) { case j: Int if j == i + 1 => () })
                        _  <- assertKyo(i2 == i + 1)
                    yield ()
                for
                    i <- Choice.evalSeq(Range(0, 100))
                    _ <- Var.run(i)(varEffect)
                yield ()
                end for
    }
end KyoUtestApiSyncTest

class KyoUtestApiAsyncTest extends TestSuite with KyoUtestApi:
    val tests = Tests {
        test("async"):
            runKyoSync:
                val varEffect =
                    for
                        i  <- Var.get[Int]
                        _  <- Var.update[Int](_ + 1)
                        i2 <- Var.get[Int]
                        _  <- assertKyo(assertMatch(i2) { case j: Int if j == i + 1 => () })
                        _  <- assertKyo(i2 == i + 1)
                    yield ()
                for
                    i <- Choice.evalSeq(Range(0, 100))
                    _ <- Var.run(i)(varEffect)
                yield ()
                end for
    }
end KyoUtestApiAsyncTest

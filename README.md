# kyo-test

Simple adapters for testing [Kyo effects](https://getkyo.io/) using standard Scala test frameworks

Currently supported frameworks: scalatest, munit, utest, and zio-test

## Install

To use kyo-test, add one of the following settings in build.sbt:

```sbt
libraryDependencies += "io.github.johnhungerford" %% "kyo-test-scalatest" % "0.0.2" % Test
libraryDependencies += "io.github.johnhungerford" %% "kyo-test-munit" % "0.0.2" % Test
libraryDependencies += "io.github.johnhungerford" %% "kyo-test-utest" % "0.0.2" % Test
libraryDependencies += "io.github.johnhungerford" %% "kyo-test-zio" % "0.0.2" % Test
```

Scala.js is supported as well, so you can use `"io.github.johnhungerford" %%% "kyo-test-scalatest" ...` if needed.

## Testing API

All bindings support the same basic API, which uses `runKyoSync` and `runKyoAsync` to convert kyo effects into native tests, and `assertKyo` to lift native assertions into a kyo effect. Assertions are lifted into a custom `Assert` effect type. 

`runKyoSync` accepts effects of type `Any < (Assert & Choice & Memo & Abort[Any] & Sync)`.

`runKyoAsync` accepts effects of type `Any < (Assert & Choice & Memo & Scope & Abort[Any] & Async)`.

`assertKyo` can always be used for boolean assertions, such as `assertKyo(someValue == 2)`. Depending on the framework, it can also be used to lift custom assertions. For instance, the following can be used in scalatest: `assertKyo(someValue shouldBe 2)`. `assertKyo` returns `Unit < Assert` and can be composed with other kyo effects to construct a test.

Taken together, a kyo test will look like the following:

```scala
// For each framework, mix in the appropriate `Kyo----Api` trait
class KyoTest extends TestFrameworkBaseSuite with KyoTestFrameworkApi:
    // This will vary by framework
    test("test-name"):
        runKyoAsync:
            // Provide dependencies for `Env` effect
            Env.runLayer(KyoService.live, OtherService.test):
                for
                    service <- Env.get[KyoService]
                    // Use Choice effect to run test on multiple input values
                    input <- Choice.evalSeq(0 to 20)
                    result <- service.doubleInput(input)
                    _ <- assertKyo(result == input * 2)
                yield ()
```

## Examples

### Scalatest

For scalatest, `runKyoSync` and `runKyoAsync` can only be used with `Any-` vs `Async-` suites:

```scala
import kyo.*
import kyo.test.KyoScalatestApi
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.*

class KyoTest extends AnyFreeSpec with Matchers with KyoScalatestApi:
    "suite" - {
        "test" in runKyoSync:
            Choice.run:
                for
                    i <- Choice.evalSeq(Range(0, 100))
                    _ <- Var.run(i):
                        for
                            i  <- Var.get[Int]
                            _  <- Var.update[Int](_ + 1)
                            i2 <- Var.get[Int]
                            _  <- assertKyo(i2 shouldBe i + 1) // *OR* assertKyo(i2 == i + 1)
                        yield ()
                yield ()
    }

class KyoAsyncTest extends AsyncFreeSpec with Matchers with KyoScalatestApi:
    "suite" - {
        "test" in runKyoAsync:
            Choice.run:
                for
                    i <- Choice.evalSeq(Range(0, 100))
                    _ <- Var.run(i):
                        for
                            i  <- Var.get[Int]
                            _  <- Var.update[Int](_ + 1)
                            i2 <- Var.get[Int]
                            _  <- assertKyo(i2 shouldBe i + 1) // *OR* assertKyo(i2 == i + 1)
                        yield ()
                yield ()
    }
```

### utest

For utest, make sure you exclude `test` from your kyo imports. Any utest test suite can accept both synchronous and asynchronous kyo tests.

```scala
import kyo.{test as ktest, *}
import utest.*

class KyoTest extends TestSuite with KyoUtestApi:
    val tests = Tests:
        test("suite"):
            test("test"):
                runKyoAsync:
                    Choice.run:
                        for
                            i <- Choice.evalSeq(Range(0, 100))
                            _ <- Var.run(i):
                                for
                                    i  <- Var.get[Int]
                                    _  <- Var.update[Int](_ + 1)
                                    i2 <- Var.get[Int]
                                    _  <- assertKyo(i2 == i + 1)
                                yield ()
                        yield ()
```

### munit

For munit, make sure you exclude `test` from your kyo imports. Any munit test suite can accept both synchronous and asynchronous kyo tests.

```scala
import kyo.{test as ktest, *}
import kyo.test.KyoMunitApi
import munit.*

class KyoTest extends FunSuite with KyoMunitApi:
    test("test"):
        runKyoAsync:
            Choice.run:
                for
                    i <- Choice.evalSeq(Range(0, 100))
                    _ <- Var.run(i):
                        for
                            i  <- Var.get[Int]
                            _  <- Var.update[Int](_ + 1)
                            i2 <- Var.get[Int]
                            _  <- assertKyo(i2 == i + 1)
                            _  <- assertKyo(assertNotEquals(i2, i)) // Custom munit assertion
                        yield ()
                yield ()
```

### zio-test

The ZIO test bindings include additional test interpreters `runKyoSyncFailFast` and `runKyoAsyncFailFast` which short-cut ZIO tests when an assertion fails. The default interpreter, to be consistent with ZIO conventions, collects all assertions. `-FailFast` variants should be used for long-running tests that might fail in multiple places.

Any zio test spec can accept both synchronous and asynchronous kyo tests. 

```scala
import kyo.*
import zio.test.*

object KyoTest extends ZIOSpecDefault with KyoZioTestApi:
    def spec =
        suite("suite")(
            test("test")(
                runKyoAsync:
                    Choice.run:
                        for
                            i <- Choice.evalSeq(Range(0, 100))
                            _ <- Var.run(i):
                                for
                                    i  <- Var.get[Int]
                                    _  <- Var.update[Int](_ + 1)
                                    i2 <- Var.get[Int]
                                    _  <- assertKyo(i2 == i + 1)
                                yield ()
                        yield ()
            ),
            test("test failing fast")(
                kyoRunAsyncFailFast:
                    Choice.run:
                        for
                            i <- Choice.evalSeq(Range(0, 100))
                            _ <- Var.run(i):
                                for
                                    i  <- Var.get[Int]
                                    _  <- Var.update[Int](_ + 1)
                                    i2 <- Var.get[Int]
                                    _  <- assertKyo(i2 == i + 1)
                                yield ()
                        yield ()
            )
        )
```

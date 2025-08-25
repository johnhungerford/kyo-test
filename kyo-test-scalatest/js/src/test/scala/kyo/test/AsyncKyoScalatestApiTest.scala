package kyo.test

import kyo.{test as ktest, *}
import kyo.test.AbstractKyoScalatestApiAsyncTest
import kyo.test.KyoScalatestApi

class KyoScalatestApiAsyncTest extends AbstractKyoScalatestApiAsyncTest:
    implicit override def executionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
end KyoScalatestApiAsyncTest

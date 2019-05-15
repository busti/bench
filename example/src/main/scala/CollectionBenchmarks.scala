package bench.example

import scala.concurrent.duration._
import bench._
import bench.util._
import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation._

import scala.collection.mutable

@js.native
@JSGlobal("Map")
class JsMap[Key, Value] extends js.Object {
  def set(key: Key, value: Value): Unit = js.native
  def delete(key: Key): Unit = js.native
  def clear(): Unit = js.native
  def get(key: Key): js.UndefOr[Value] = js.native
  def has(key: Key): Boolean = js.native
  def keys: Iterator[Key] = js.native
  def values: Iterator[Value] = js.native
  def entries: Iterator[js.Array[Key | Value]] = js.native
  def forEach(f: (Key, Value) => Unit): Unit = js.native
  def size: Int = js.native
}

@js.native
trait DictionaryRawApply[A] extends js.Object {
  @JSBracketAccess
  def apply(key: String): js.UndefOr[A] = js.native
}

case class Foo(a: Long, b: Long)

object CollectionBenchmarks {
  val hashmap = Comparison("Map", Seq(
    Benchmark[(js.Dictionary[Int], Int)](
      "js.dictionary string",
      { n =>
        val map = js.Dictionary[Int]()
        (0 to n).foreach { i => map += i.toString -> i }
        (map, n)
      },
      { case (map, n) =>
        (0 to n).foreach { i => map(i.toString) }
      }
    ),
    Benchmark[(js.Dictionary[Int], Int)](
      "js.dictionary raw string",
      { n =>
        val map = js.Dictionary[Int]()
        (0 to n).foreach { i => map += i.toString -> i }
        (map, n)
      },
      { case (map, n) =>
        (0 to n).foreach { i => map.asInstanceOf[DictionaryRawApply[Int]].apply(i.toString) }
      }
    ),
    Benchmark[(JsMap[String, Int], Int)](
      "js.map string",
      { n =>
        val map = new JsMap[String, Int]()
        (0 to n).foreach { i => map.set(i.toString, i) }
        (map, n)
      },
      { case (map, n) =>
        (0 to n).foreach { i => map.get(i.toString) }
      }
    ),
    Benchmark[(mutable.HashMap[String, Int], Int)](
      "mutable.hashmap string",
      { n =>
        val map = new mutable.HashMap[String, Int]
        (0 to n).foreach { i => map += i.toString -> i }
        (map, n)
      },
      { case (map, n) =>
        (0 to n).foreach { i => map(i.toString) }
      }
    ),
    Benchmark[(Map[String, Int], Int)](
      "immutable.map string",
      { n =>
        var map = Map[String, Int]()
        (0 to n).foreach { i => map += i.toString -> i }
        (map, n)
      },
      { case (map, n) =>
        (0 to n).foreach { i => map(i.toString) }
      }
    ),
    Benchmark[(js.Dictionary[Int], Int)](
      "js.dictionary raw case class",
      { n =>
        val map = js.Dictionary[Int]()
        (0 to n).foreach { i => map += Foo(i,i).hashCode.toString -> i }
        (map, n)
      },
      { case (map, n) =>
        (0 to n).foreach { i => map.asInstanceOf[DictionaryRawApply[Int]].apply(Foo(i,i).hashCode.toString) }
      }
    ),
    Benchmark[(JsMap[Int, Int], Int)](
      "js.map case class",
      { n =>
        val map = new JsMap[Int, Int]()
        (0 to n).foreach { i => map.set(Foo(i,i).hashCode, i) }
        (map, n)
      },
      { case (map, n) =>
        (0 to n).foreach { i => map.get(Foo(i,i).hashCode) }
      }
    ),
    Benchmark[(mutable.HashMap[Foo, Int], Int)](
      "mutable.hashmap case class",
      { n =>
        val map = new mutable.HashMap[Foo, Int]
        (0 to n).foreach { i => map += Foo(i,i) -> i }
        (map, n)
      },
      { case (map, n) =>
        (0 to n).foreach { i => map(Foo(i,i)) }
      }
    ),
  ))

  val hashmapBuild = Comparison("MapBuild", Seq(
    Benchmark[Int](
      "js.dictionary string",
      n => n,
      { n =>
        val map = js.Dictionary[Int]()
        (0 to n).foreach { i => map += i.toString -> i }
        map
      },
    ),
    Benchmark[Int](
      "js.map string",
      n => n,
      { n =>
        val map = new JsMap[String, Int]()
        (0 to n).foreach { i => map.set(i.toString, i) }
        map
      },
    ),
    Benchmark[Int](
      "mutable.hashmap string",
      n => n,
      { n =>
        val map = new mutable.HashMap[String, Int]
        (0 to n).foreach { i => map += i.toString -> i }
        map
      },
    ),
    Benchmark[Int](
      "immutable.map string",
      n => n,
      { n =>
        var map = Map[String, Int]()
        (0 to n).foreach { i => map += i.toString -> i }
        map
      },
    ),
    Benchmark[Int](
      "js.dictionary case class",
      n => n,
      { n =>
        val map = js.Dictionary[Int]()
        (0 to n).foreach { i => map += Foo(i,i).hashCode.toString -> i }
        map
      },
    ),
    Benchmark[Int](
      "js.map case class",
      n => n,
      { n =>
        val map = new JsMap[Int, Int]()
        (0 to n).foreach { i => map.set(Foo(i,i).hashCode, i) }
        map
      },
    ),
    Benchmark[Int](
      "mutable.hashmap case class",
      n => n,
      { n =>
        val map = new mutable.HashMap[Foo, Int]
        (0 to n).foreach { i => map += Foo(i,i) -> i }
        map
      },
    ),
  ))


  val linearScan = Comparison("Linear Scan", Seq(
    Benchmark[mutable.HashSet[Int]](
      "mutable set foreach",
      n => new mutable.HashSet[Int]() ++ Array.fill[Int](n)(rInt),
      { set =>
        var sum: Int = 0
        set.foreach(sum += _)
        sum
      }
    ),
    Benchmark[Set[Int]](
      "immutable set foreach",
      n => Array.fill[Int](n)(rInt).toSet,
      { set =>
        var sum: Int = 0
        set.foreach(sum += _)
        sum
      }
    ),
    Benchmark[js.Array[Int]](
      "js.array foreach",
      n => Array.fill[Int](n)(rInt).toJSArray,
      { (arr) =>
        var sum: Int = 0
        arr.foreach(sum += _)
        sum
      }
    ),
    Benchmark[Array[Int]](
      "array foreach",
      n => Array.fill[Int](n)(rInt),
      { (arr) =>
        var sum: Int = 0
        arr.foreach(sum += _)
        sum
      }
    ),
    Benchmark[Array[Int]](
      "array while",
      n => Array.fill(n)(rInt),
      { (arr) =>
        var sum: Int = 0
        var i = 0
        while (i < arr.length) {
          sum += arr(i)
          i += 1
        }
        sum
      }
    )
  ))

  val construct = Comparison("Construct", Seq(
    Benchmark[Int](
      "mutable set",
      n => n,
      { n =>
        val set = mutable.HashSet[String]()
        var i = 0
        while (i < n) {
          set += i.toString
          i += 1
        }
        set
      }
    ),
    Benchmark[Int](
      "immutable set",
      n => n,
      { n =>
        var set = Set[String]()
        var i = 0
        while (i < n) {
          set += i.toString
          i += 1
        }
        set
      }
    ),
    Benchmark[Int](
      "array",
      n => n,
      { n =>
        val arr = new Array[String](n)
        var i = 0
        while (i < n) {
          arr(i) = i.toString
          i += 1
        }
        arr
      }
    ),
    Benchmark[Int](
      "arraybuffer",
      n => n,
      { n =>
        val arr = mutable.ArrayBuffer[String]()
        var i = 0
        while (i < n) {
          arr += i.toString
          i += 1
        }
        arr
      }
    )
  ))

  val map = Comparison("Map", Seq(
    Benchmark[mutable.HashSet[Int]](
      "mutable set",
      n => new mutable.HashSet[Int]() ++ Array.fill[Int](n)(rInt),
      { set =>
        set.map(_ * 10)
      }
    ),
    Benchmark[Set[Int]](
      "immutable set",
      n => Array.fill[Int](n)(rInt).toSet,
      { set =>
        set.map(_ * 10)
      }
    ),
    Benchmark[Array[Int]](
      "array",
      n => Array.fill[Int](n)(rInt),
      { (arr) =>
        arr.map(_ * 10)
      }
    ),
    Benchmark[Array[Int]](
      "array while",
      n => Array.fill(n)(rInt),
      { (arr) =>
        val arr2 = new Array[Int](arr.length)
        var i = 0
        while (i < arr.length) {
          arr2(i) += arr(i) * 10
          i += 1
        }
        arr
      }
    )
  ))

  val lookup = Comparison("Lookup", Seq(
    Benchmark[(Int, mutable.HashSet[Int])](
      "mutable set",
      n => (n, new mutable.HashSet[Int]() ++ Array.range(0, n)),
      { case (n, set) =>
        set.contains(n)
      }
    ),
    Benchmark[(Int, Set[Int])](
      "immutable set",
      n => (n, Array.range(0, n).toSet),
      { case (n, set) =>
        set.contains(n)
      }
    ),
    Benchmark[(Int, Array[Int])](
      "array contains",
      n => (n, Array.range(0, n)),
      { case (n, arr) =>
        arr.contains(n)
      }
    ),
    Benchmark[(Int, Array[Int])](
      "array exists",
      n => (n, Array.range(0, n)),
      { case (n, arr) =>
        arr.exists(_ == n)
      }
    ),
    Benchmark[(Int, Array[Int])](
      "array while",
      n => (n, Array.range(0, n)),
      { case (n, arr) =>
        var i = 0
        var found = false
        while (!found && i < arr.length) {
          if (arr(i) == n) found = true
          i += 1
        }
        found
      }
    )
  ))
}

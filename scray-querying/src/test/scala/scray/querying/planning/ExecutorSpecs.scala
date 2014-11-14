// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package scray.querying.planning

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import scray.querying.description.SimpleRow
import scray.querying.description.TableIdentifier
import scray.querying.description.Column
import scray.querying.description.RowColumn
import com.twitter.util.Future
import scray.querying.description.Row
import scray.querying.description.ColumnOrdering
import com.twitter.util.Await
import com.twitter.concurrent.Spool
import scala.annotation.tailrec

@RunWith(classOf[JUnitRunner])
class ScrayQueryingExecutorTest extends WordSpec {

  val ti = TableIdentifier("cassandra", "mytestspace", "mycf")
  
  val cols = 1.until(10).map(i => Column(s"bla$i", ti))

  val sr1 = SimpleRow(List(RowColumn(cols(0), 34), RowColumn(cols(1), 21)))  
  val sr2 = SimpleRow(List(RowColumn(cols(1), 12), RowColumn(cols(2), "guck")))  
  val sr3 = SimpleRow(List(RowColumn(cols(0), 56), RowColumn(cols(1), 34), RowColumn(cols(2), 456)))  
  val sr4 = SimpleRow(List(RowColumn(cols(0), 1), RowColumn(cols(1), 34.4f)))  
  val sr5 = SimpleRow(List(RowColumn(cols(0), 33), RowColumn(cols(1), 21)))    
  val sr6 = SimpleRow(List(RowColumn(cols(0), 34), RowColumn(cols(1), "dffg")))  
  val sr7 = SimpleRow(List())  
  val sr8 = SimpleRow(List(RowColumn(cols(0), 100), RowColumn(cols(1), "dffg")))  
  
  // these seqs are ordered
  val seq1 = Future(Seq(sr4, sr1, sr3))
  val seq2 = Future(Seq(sr4, sr5, sr8))
  val seq3 = Future(Seq(sr8, sr7))
  val seq4 = Future(Seq(sr8, sr2))
  
  // these spools are ordered
  val spool1 = Future(sr4 **:: sr1 **:: sr3 **:: Spool.empty[Row])
  val spool2 = Future(sr4 **:: sr5 **:: sr8 **:: Spool.empty[Row])
  val spool3 = Future(sr8 **:: sr7 **:: Spool.empty[Row])
  val spool4 = Future(sr8 **:: sr2 **:: Spool.empty[Row])
  
  val queryOrdering = Some(ColumnOrdering[Int](cols(0))(implicitly[Ordering[Int]]))
  
  // empty rows or rows without column in question are greater than the others
  val compRows: (Row, Row) => Boolean = 
    scray.querying.source.rowCompWithOrdering(queryOrdering.get.column, queryOrdering.get.ordering)
  
  @tailrec private def countAndPrintOrdered(fut: Future[Spool[Row]], number: Int, lastRow: Option[Row]): Unit = {
    if(number >= 0) {
      val spool = Await.result(fut)
      val row = spool.head
      if(lastRow.isDefined) {
        assert(compRows(lastRow.get, row))
      }
      countAndPrintOrdered(spool.tail, number - 1, Some(row))
    }
  }

  @tailrec private def countAndPrintUnordered(fut: Future[Spool[Row]], number: Int, lastRow: Option[Row]): Unit = {
    if(number >= 0) {
      val spool = Await.result(fut)
      val row = spool.head
      countAndPrintUnordered(spool.tail, number - 1, Some(row))
    }
  }

  
  "Scray's executor with ordered seq and columns" should {
    "return empty if no results are provided" in {
      val result = MergingResultSpool.mergeOrderedSpools(Seq(), Seq(), compRows, Seq())
      assert(Await.result(result) == Spool.empty)
    }
    "combine sorted seqs" in {
      val result = MergingResultSpool.mergeOrderedSpools(Seq(seq1, seq2), Seq(), compRows, Seq(0,0))
      val fut1 = Await.result(result) 
      val fut2 = Await.result(fut1.tail) 
      val fut3 = Await.result(fut2.tail) 
      val fut4 = Await.result(fut3.tail) 
      val fut5 = Await.result(fut4.tail) 
      val fut6 = Await.result(fut5.tail) 
      assert(fut1.head.getColumnValue[Int](cols(0)).get == 1)
      assert(fut2.head.getColumnValue[Int](cols(0)).get == 1)
      assert(fut3.head.getColumnValue[Int](cols(0)).get == 33)
      assert(fut4.head.getColumnValue[Int](cols(0)).get == 34)
      assert(fut5.head.getColumnValue[Int](cols(0)).get == 56)
      assert(fut6.head.getColumnValue[Int](cols(0)).get == 100)
    }
    "combine sorted seqs with wholes" in {
      val result = MergingResultSpool.mergeOrderedSpools(Seq(seq1, seq3), Seq(), compRows, Seq(0,0))
      val fut1 = Await.result(result) 
      val fut2 = Await.result(fut1.tail) 
      val fut3 = Await.result(fut2.tail) 
      val fut4 = Await.result(fut3.tail) 
      val fut5 = Await.result(fut4.tail) 
      assert(fut4.head.getColumnValue[Int](cols(0)).get == 100)
      assert(fut5.head.getColumnValue[Int](cols(0)) == None)
    }
    "combine sorted seqs with rows with uncomparable columns" in {
      val result = MergingResultSpool.mergeOrderedSpools(Seq(seq1, seq4), Seq(), compRows, Seq(0,0))
      val fut1 = Await.result(result) 
      val fut2 = Await.result(fut1.tail) 
      val fut3 = Await.result(fut2.tail) 
      val fut4 = Await.result(fut3.tail) 
      val fut5 = Await.result(fut4.tail) 
      assert(fut4.head.getColumnValue[Int](cols(0)).get == 100)
      assert(fut5.head.getColumnValue[Int](cols(0)) == None)
    }
    "combine sorted spools" in {
      val result = MergingResultSpool.mergeOrderedSpools(Seq(), Seq(spool1, spool2), compRows, Seq())
      val fut1 = Await.result(result) 
      val fut2 = Await.result(fut1.tail) 
      val fut3 = Await.result(fut2.tail) 
      val fut4 = Await.result(fut3.tail) 
      val fut5 = Await.result(fut4.tail) 
      val fut6 = Await.result(fut5.tail) 
      assert(fut1.head.getColumnValue[Int](cols(0)).get == 1)
      assert(fut2.head.getColumnValue[Int](cols(0)).get == 1)
      assert(fut3.head.getColumnValue[Int](cols(0)).get == 33)
      assert(fut4.head.getColumnValue[Int](cols(0)).get == 34)
      assert(fut5.head.getColumnValue[Int](cols(0)).get == 56)
      assert(fut6.head.getColumnValue[Int](cols(0)).get == 100)
    }
    "combine sorted spools with wholes" in {
      val result = MergingResultSpool.mergeOrderedSpools(Seq(), Seq(spool1, spool3), compRows, Seq())
      val fut1 = Await.result(result) 
      val fut2 = Await.result(fut1.tail) 
      val fut3 = Await.result(fut2.tail) 
      val fut4 = Await.result(fut3.tail) 
      val fut5 = Await.result(fut4.tail) 
      assert(fut4.head.getColumnValue[Int](cols(0)).get == 100)
      assert(fut5.head.getColumnValue[Int](cols(0)) == None)
    }
    "combine sorted spools with rows with uncomparable columns" in {
      val result = MergingResultSpool.mergeOrderedSpools(Seq(), Seq(spool1, spool4), compRows, Seq())
      val fut1 = Await.result(result) 
      val fut2 = Await.result(fut1.tail) 
      val fut3 = Await.result(fut2.tail) 
      val fut4 = Await.result(fut3.tail) 
      val fut5 = Await.result(fut4.tail) 
      assert(fut4.head.getColumnValue[Int](cols(0)).get == 100)
      assert(fut5.head.getColumnValue[Int](cols(0)) == None)
    }
    "combine sorted spools and seqs at once" in {
      val result = MergingResultSpool.mergeOrderedSpools(
          Seq(seq1, seq2, seq3, seq4), Seq(spool1, spool2, spool3, spool4), compRows, Seq(0,0,0,0))
      countAndPrintOrdered(result, 19, None)
    }
  }
  "Scray's executor with unordered seq and columns" should {
    "return empty if no results are provided" in {
      val result = MergingResultSpool.mergeUnorderedResults(Seq(), Seq(), Seq())
      assert(Await.result(result) == Spool.empty)
    }
    "combine seqs" in {
      val result = MergingResultSpool.mergeUnorderedResults(Seq(seq1, seq2), Seq(), Seq(0,0))
      countAndPrintUnordered(result, 5, None)
    }
    "combine seqs with wholes" in {
      val result = MergingResultSpool.mergeUnorderedResults(Seq(seq1, seq3), Seq(), Seq(0,0))
      countAndPrintUnordered(result, 4, None)
    }
    "combine spools" in {
      val result = MergingResultSpool.mergeUnorderedResults(Seq(), Seq(spool1, spool2), Seq())
      countAndPrintUnordered(result, 5, None)
    }
    "combine spools with wholes" in {
      val result = MergingResultSpool.mergeUnorderedResults(Seq(), Seq(spool1, spool3), Seq())
      countAndPrintUnordered(result, 4, None)
    }
    "combine sorted spools and seqs at once" in {
      val result = MergingResultSpool.mergeUnorderedResults(
          Seq(seq1, seq2, seq3, seq4), Seq(spool1, spool2, spool3, spool4), Seq(0,0,0,0))
      countAndPrintUnordered(result, 19, None)
    }

  }
}
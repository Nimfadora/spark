/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.status.api.v1.sql

import javax.ws.rs._
import javax.ws.rs.core.MediaType

import scala.collection.JavaConverters._

import org.apache.spark.status.api.v1.NotFoundException

@Produces(Array(MediaType.APPLICATION_JSON))
private[v1] class StreamingApiRootResource extends BaseStreamingAppResource {
  //  val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  @GET
  @Path("batches")
  def batchesList: Seq[BatchInfo] = withQueryListener(_.getBatchList())

//  @GET
//  @Path("batches/stats")
//  def batchesStats: Seq[Int] = {
//    withQueryListener { listener =>
//      Seq(listener.qm.active.length, listener.numQueries(), listener.numInProgress(),
//        listener.numFinished(), listener.activeQueriesNum(), listener.realActiveQueriesNum())
//    }
//  }
  //   startedQueries: 1
  //   queriesProgressEventsCount: 26
  //   finishedQueries: 0


  @GET
  @Path("batches/{batchId: \\d+}")
  def oneBatch(@PathParam("batchId") batchId: Long): BatchInfo = {
    batchesList.find {
      _.batchId == batchId
    }.getOrElse(
      throw new NotFoundException("unknown batch: " + batchId))
  }

  private def avgRate(data: Seq[Double]): Option[Double] = {
    if (data.isEmpty) None else Some(data.sum / data.size)
  }

  private def avgTime(data: Seq[Long]): Option[Long] = {
    if (data.isEmpty) None else Some(data.sum / data.size)
  }

}

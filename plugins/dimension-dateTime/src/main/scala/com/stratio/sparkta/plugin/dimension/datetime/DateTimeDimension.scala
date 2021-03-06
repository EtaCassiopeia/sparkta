/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.dimension.datetime

import java.io.{Serializable => JSerializable}
import java.util.Date

import akka.event.slf4j.SLF4JLogging
import org.joda.time.DateTime

import com.stratio.sparkta.plugin.dimension.datetime.DateTimeDimension._
import com.stratio.sparkta.sdk._

case class DateTimeDimension(props: Map[String, JSerializable]) extends Bucketer with JSerializable with SLF4JLogging {

  def this() {
    this(Map())
  }

  override val defaultTypeOperation = TypeOp.Timestamp

  override val properties: Map[String, JSerializable] = props ++ {
    if (!props.contains(GranularityPropertyName)) Map(GranularityPropertyName -> DefaultGranularity) else Map()
  }

  override val bucketTypes: Map[String, BucketType] = Map(
    timestamp.id -> timestamp,
    SecondName -> getPrecision(SecondName, getTypeOperation(SecondName)),
    MinuteName -> getPrecision(MinuteName, getTypeOperation(MinuteName)),
    HourName -> getPrecision(HourName, getTypeOperation(HourName)),
    DayName -> getPrecision(DayName, getTypeOperation(DayName)),
    MonthName -> getPrecision(MonthName, getTypeOperation(MonthName)),
    YearName -> getPrecision(YearName, getTypeOperation(YearName)))

  @throws(classOf[ClassCastException])
  override def bucket(value: JSerializable): Map[BucketType, JSerializable] =
    try {
      bucketTypes.map { case (name, bucketType) =>
        bucketType -> DateTimeDimension.bucket(value.asInstanceOf[Date], bucketType, properties)
      }
    }
    catch {
      case cce: ClassCastException => {
        log.error("Error parsing " + value + " .")
        throw cce
      }
    }
}

object DateTimeDimension {

  final val DefaultGranularity = "second"
  final val GranularityPropertyName = "granularity"
  final val SecondName = "second"
  final val MinuteName = "minute"
  final val HourName = "hour"
  final val DayName = "day"
  final val MonthName = "month"
  final val YearName = "year"
  final val timestamp = Bucketer.getTimestamp(Some(TypeOp.Timestamp), TypeOp.Timestamp)

  def bucket(value: Date, bucketType: BucketType, properties: Map[String, JSerializable]): JSerializable = {
    DateOperations.dateFromGranularity(new DateTime(value), bucketType match {
      case t if t == timestamp => if (properties.contains(GranularityPropertyName))
        properties.get(GranularityPropertyName).get.toString
      else DefaultGranularity
      case _ => bucketType.id
    }).asInstanceOf[JSerializable]
  }
}

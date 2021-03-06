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

package com.stratio.sparkta.plugin.dimension.tag

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging

import com.stratio.sparkta.plugin.dimension.tag.TagDimension._
import com.stratio.sparkta.sdk.{BucketType, Bucketer, TypeOp}

case class TagDimension(props: Map[String, JSerializable]) extends Bucketer with SLF4JLogging {

  def this() {
    this(Map())
  }

  override val defaultTypeOperation = TypeOp.String

  override val properties: Map[String, JSerializable] = props

  override val bucketTypes: Map[String, BucketType] = Map(
    FirstTagName -> getPrecision(FirstTagName, getTypeOperation(FirstTagName)),
    LastTagName -> getPrecision(LastTagName, getTypeOperation(LastTagName)),
    AllTagsName -> getPrecision(AllTagsName, getTypeOperation(AllTagsName)))

  override def bucket(value: JSerializable): Map[BucketType, JSerializable] =
    bucketTypes.map(bt => bt._2 -> TagDimension.bucket(value.asInstanceOf[Iterable[JSerializable]], bt._2))
}

object TagDimension {

  final val FirstTagName = "fistTag"
  final val LastTagName = "lastTag"
  final val AllTagsName = "allTags"

  def bucket(value: Iterable[JSerializable], bucketType: BucketType): JSerializable =
    bucketType.id match {
      case name if name == FirstTagName => value.head
      case name if name == LastTagName => value.last
      case name if name == AllTagsName => value.toSeq.asInstanceOf[JSerializable]
    }
}

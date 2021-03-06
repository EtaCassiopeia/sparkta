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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}
import scala.util.Try

import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp

abstract class Operator(properties: Map[String, JSerializable]) extends Parameterizable(properties)
with Ordered[Operator] {

  def typeOp: Option[TypeOp] = None

  def key: String

  def writeOperation: WriteOp

  def processMap(inputFields: Map[String, JSerializable]): Option[Any]

  def processReduce(values: Iterable[Option[Any]]): Option[Any]

  def returnType: TypeOp = typeOp.getOrElse(TypeOp.Binary)

  def compare(operator: Operator): Int = key compareTo operator.key

  //scalastyle:off
  def getNumberFromSerializable(value: JSerializable): Option[Number] =
    value match {
      case value if value.isInstanceOf[String] => Try(Some(value.asInstanceOf[String].toDouble.asInstanceOf[Number]))
        .getOrElse(None)
      case value if value.isInstanceOf[Int] ||
        value.isInstanceOf[Double] ||
        value.isInstanceOf[Float] ||
        value.isInstanceOf[Long] ||
        value.isInstanceOf[Short] ||
        value.isInstanceOf[Byte] => Some(value.asInstanceOf[Number])
      case _ => None
    }

  //scalastyle:on
}
